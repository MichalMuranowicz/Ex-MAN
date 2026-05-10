import json
import time
from pathlib import Path

from selenium import webdriver
from selenium.webdriver.chrome.options import Options


LOGIN_URL = "https://motos.man.eu/motos/frame/frame_en.html"
OUT_DIR = Path(__file__).resolve().parent / "desktop-test-output"
OUT_DIR.mkdir(exist_ok=True)


INSPECT_SCRIPT = r"""
return (function() {
  function collect(win, path, out) {
    try {
      const doc = win.document;
      const bodyText = (doc.body && doc.body.innerText) ? doc.body.innerText : '';
      const rows = Array.from(doc.querySelectorAll('tr')).map(function(row) {
        return row.innerText || '';
      });
      out.frames.push({
        path: path,
        url: win.location.href,
        title: doc.title || '',
        text: bodyText,
        rows: rows
      });

      for (let i = 0; i < win.frames.length; i++) {
        collect(win.frames[i], path + '/frame[' + i + ']', out);
      }
    } catch (e) {
      out.frames.push({
        path: path,
        error: String(e),
        text: '',
        rows: []
      });
    }
  }

  const out = { frames: [] };
  collect(window, 'top', out);

  const bodyText = out.frames.map(function(frame) { return frame.text || ''; }).join('\n');
  const rows = [];
  out.frames.forEach(function(frame) {
    (frame.rows || []).forEach(function(rowText) {
      rows.push({ frame: frame.path, text: rowText });
    });
  });

  const code20Rows = rows.filter(function(row) {
    const text = row.text || '';
    return /(^|\s)20(\s|$)/.test(text);
  });

  const units = [];
  out.frames.forEach(function(frame) {
    try {
      const win = frame.path === 'top' ? window : null;
    } catch (e) {}
  });

  function collectUnits(win, path) {
    try {
      const doc = win.document;
      Array.from(doc.querySelectorAll('tr')).forEach(function(row) {
        const cells = Array.from(row.querySelectorAll('td')).map(function(cell) {
          return (cell.innerText || '').trim();
        });
        const nonEmpty = cells.filter(Boolean);
        if (nonEmpty[0] !== '20') return;
        if (!/^[A-Z0-9]{6,8}$/.test(nonEmpty[1] || '')) return;

        const link = row.querySelector("a[href*='transportDetails'], a[href*='transport']");
        units.push({
          frame: path,
          vehicleNo: nonEmpty[1] || '',
          pickupCode: nonEmpty[2] || '',
          pickupDate: nonEmpty[3] || '',
          country: nonEmpty[4] || '',
          dest: nonEmpty[5] || '',
          toNo: nonEmpty[16] || '',
          href: link ? link.href : '',
          cells: nonEmpty
        });
      });
      for (let i = 0; i < win.frames.length; i++) {
        collectUnits(win.frames[i], path + '/frame[' + i + ']');
      }
    } catch (e) {}
  }
  collectUnits(window, 'top');

  return {
    url: location.href,
    title: document.title,
    hasVehicNo: bodyText.indexOf('Vehic. no') !== -1,
    hasToDetails: bodyText.indexOf('TO details') !== -1,
    hasPickup: bodyText.indexOf('Pick up') !== -1,
    frames: out.frames.map(function(frame) {
      return {
        path: frame.path,
        url: frame.url || '',
        title: frame.title || '',
        textLength: (frame.text || '').length,
        rows: (frame.rows || []).length,
        error: frame.error || ''
      };
    }),
    rows: rows.length,
    code20Rows: code20Rows.length,
    firstCode20Rows: code20Rows.slice(0, 5),
    units: units,
    firstUnits: units.slice(0, 10),
    sample: bodyText.slice(0, 1500)
  };
})();
"""


def yes_no(value):
    return "TAK" if value else "NIE"


def main():
    print("Ex MAN Android - desktopowy test WebView")
    print("Otwieram Chrome w trybie mobilnym. Zaloguj sie w portalu MAN.")
    print("Gdy dojdziesz do listy jednostek, wroc do tego okna i nacisnij Enter.")
    print()

    options = Options()
    options.add_argument("--start-maximized")
    options.add_experimental_option(
        "mobileEmulation",
        {
            "deviceMetrics": {"width": 412, "height": 915, "pixelRatio": 2.625},
            "userAgent": (
                "Mozilla/5.0 (Linux; Android 13; Pixel 7) "
                "AppleWebKit/537.36 (KHTML, like Gecko) "
                "Chrome/120.0.0.0 Mobile Safari/537.36 ExMANAndroid/0.1"
            ),
        },
    )

    driver = webdriver.Chrome(options=options)
    try:
        driver.get(LOGIN_URL)
        input("Po zalogowaniu i przejsciu do listy jednostek nacisnij Enter...")

        result = driver.execute_script(INSPECT_SCRIPT)
        result_path = OUT_DIR / "webview_inspect_result.json"
        screenshot_path = OUT_DIR / "webview_test_screenshot.png"

        result_path.write_text(json.dumps(result, ensure_ascii=False, indent=2), encoding="utf-8")
        driver.save_screenshot(str(screenshot_path))

        print()
        print("Wynik testu:")
        print(f"URL: {result.get('url')}")
        print(f"Tytul: {result.get('title')}")
        print(f"Widze 'Vehic. no': {yes_no(result.get('hasVehicNo'))}")
        print(f"Widze 'TO details': {yes_no(result.get('hasToDetails'))}")
        print(f"Widze 'Pick up': {yes_no(result.get('hasPickup'))}")
        print(f"Liczba wierszy HTML: {result.get('rows')}")
        print(f"Podejrzane wiersze z kodem 20: {result.get('code20Rows')}")
        print(f"Rekordy kod 20: {len(result.get('units', []))}")
        for unit in result.get("firstUnits", []):
            print(
                "  "
                f"{unit.get('vehicleNo')} | pickup={unit.get('pickupCode')} "
                f"{unit.get('pickupDate')} | dest={unit.get('country')} {unit.get('dest')} "
                f"| href={'TAK' if unit.get('href') else 'NIE'}"
            )
        print("Ramki:")
        for frame in result.get("frames", []):
            print(
                f"  {frame.get('path')} | rows={frame.get('rows')} | "
                f"text={frame.get('textLength')} | {frame.get('url') or frame.get('error')}"
            )
        print()
        print(f"Zapisano wynik: {result_path}")
        print(f"Zapisano zrzut: {screenshot_path}")
        print()
        print("Probka tekstu:")
        print((result.get("sample") or "").replace("\n", " | "))

        first_href = ""
        for unit in result.get("units", []):
            if unit.get("href"):
                first_href = unit["href"]
                break

        if first_href:
            print()
            answer = input("Znaleziono link do szczegolow. Przetestowac pierwszy rekord? [T/n] ").strip().lower()
            if answer in ["", "t", "tak", "y", "yes"]:
                inspect_details(driver, first_href)
        print()
        input("Nacisnij Enter, aby zamknac Chrome...")
    finally:
        driver.quit()


DETAILS_SCRIPT = r"""
return (function() {
  function collect(win, path, out) {
    try {
      const doc = win.document;
      const bodyText = (doc.body && doc.body.innerText) ? doc.body.innerText : '';
      out.frames.push({
        path: path,
        url: win.location.href,
        title: doc.title || '',
        text: bodyText
      });
      for (let i = 0; i < win.frames.length; i++) {
        collect(win.frames[i], path + '/frame[' + i + ']', out);
      }
    } catch (e) {
      out.frames.push({path:path, error:String(e), text:''});
    }
  }

  function valueAfterLabel(text, labels) {
    const lines = text.split(/\r?\n/).map(function(x) { return x.trim(); }).filter(Boolean);
    for (let i = 0; i < lines.length; i++) {
      for (const label of labels) {
        if (lines[i] === label && i + 1 < lines.length) return lines[i + 1];
        if (lines[i].startsWith(label)) {
          const value = lines[i].slice(label.length).trim();
          if (value) return value;
        }
      }
    }
    return '';
  }

  function splitAddressLine(line) {
    const parts = (line || '').trim().split(/\s+/);
    if (parts.length >= 3) {
      return {
        country: parts[0] || '',
        zipcode: parts[1] || '',
        city: parts.slice(2).join(' ')
      };
    }
    if (parts.length === 2) {
      return {country: parts[0] || '', zipcode: '', city: parts[1] || ''};
    }
    return {country: '', zipcode: '', city: line || ''};
  }

  function parseAddressBlock(lines) {
    const last = splitAddressLine(lines[2] || '');
    return {
      name: lines[0] || '',
      street: lines[1] || '',
      country: last.country,
      zipcode: last.zipcode,
      city: last.city
    };
  }

  function extractAddresses(text) {
    const lines = text.split(/\r?\n/).map(function(x) { return x.trim(); }).filter(Boolean);
    const result = {pickup: {}, delivery: {}};

    for (let i = 0; i < lines.length; i++) {
      if (lines[i] !== 'Pick up:') continue;
      if (lines[i + 2] !== 'Delivery:') continue;

      const start = i + 4;
      const pickupLines = lines.slice(start, start + 3);
      const deliveryLines = lines.slice(start + 3, start + 6);

      if (pickupLines.length === 3 && deliveryLines.length === 3) {
        result.pickup = parseAddressBlock(pickupLines);
        result.delivery = parseAddressBlock(deliveryLines);
        return result;
      }
    }

    return result;
  }

  function extractTelephone(text) {
    const lines = text.split(/\r?\n/).map(function(x) { return x.trim(); }).filter(Boolean);
    const numbers = [];
    for (let i = 0; i < lines.length; i++) {
      if (lines[i] === 'Telephone number:' && i + 1 < lines.length) {
        const value = lines[i + 1];
        if (value && value !== 'Fax number:') numbers.push(value);
      }
    }
    return numbers.length ? numbers[numbers.length - 1] : '';
  }

  const out = {frames:[]};
  collect(window, 'top', out);
  const text = out.frames.map(function(frame) { return frame.text || ''; }).join('\n');
  const addresses = extractAddresses(text);

  return {
    url: location.href,
    frames: out.frames.map(function(frame) {
      return {
        path: frame.path,
        url: frame.url || '',
        textLength: (frame.text || '').length,
        error: frame.error || ''
      };
    }),
    hasToDetails: text.indexOf('TO details') !== -1,
    hasVehicleDetails: text.indexOf('Vehicle details') !== -1,
    typeCab: valueAfterLabel(text, ['Type / cab:', 'Type / cab']),
    vin: valueAfterLabel(text, ['VIN:', 'VIN']),
    totalLength: valueAfterLabel(text, ['Total length:', 'Total length']),
    height: valueAfterLabel(text, ['Height:', 'Height']),
    weight: valueAfterLabel(text, ['Weight:', 'Weight']),
    vehicleType: valueAfterLabel(text, ['Vehicle type:', 'Vehicle type']),
    pickupUntil: valueAfterLabel(text, ['Pick up until:', 'Pick up untill:', 'Pick up until', 'Pick up untill']),
    latestDelivery: valueAfterLabel(text, ['Latest delivery:', 'Latest delivery']),
    telephone: extractTelephone(text),
    pickupAddress: addresses.pickup,
    deliveryAddress: addresses.delivery,
    sample: text.slice(0, 1800)
  };
})();
"""


CLICK_TAB_SCRIPT = r"""
return (function(label) {
  function clickIn(win) {
    try {
      const doc = win.document;
      const candidates = Array.from(doc.querySelectorAll('a, span, td, div, input, button'));
      for (const el of candidates) {
        const text = ((el.innerText || el.value || '') + '').trim();
        if (text !== label) continue;

        const clickable = el.closest('a') || el;
        clickable.click();
        return true;
      }

      for (let i = 0; i < win.frames.length; i++) {
        if (clickIn(win.frames[i])) return true;
      }
    } catch (e) {}
    return false;
  }

  return clickIn(window);
})(arguments[0]);
"""


def inspect_details(driver, href):
    print()
    print("Otwieram szczegoly pierwszego rekordu...")
    driver.get(href)
    time.sleep(2)

    clicked_to = driver.execute_script(CLICK_TAB_SCRIPT, "TO details")
    print(f"Klik TO details: {yes_no(clicked_to)}")
    time.sleep(1.2)

    result = driver.execute_script(DETAILS_SCRIPT)
    result_path = OUT_DIR / "details_to_details_result.json"
    screenshot_path = OUT_DIR / "details_to_details_screenshot.png"

    result_path.write_text(json.dumps(result, ensure_ascii=False, indent=2), encoding="utf-8")
    driver.save_screenshot(str(screenshot_path))

    print("Wynik szczegolow:")
    print(f"URL: {result.get('url')}")
    print(f"Widze TO details: {yes_no(result.get('hasToDetails'))}")
    print(f"Widze Vehicle details: {yes_no(result.get('hasVehicleDetails'))}")
    print(f"Type / cab: {result.get('typeCab')}")
    print(f"VIN: {result.get('vin')}")
    print(f"Total length: {result.get('totalLength')}")
    print(f"Height: {result.get('height')}")
    print(f"Weight: {result.get('weight')}")
    print(f"Pick up until: {result.get('pickupUntil')}")
    print(f"Latest delivery: {result.get('latestDelivery')}")
    print(f"Telephone: {result.get('telephone')}")
    print(f"Pick up address: {result.get('pickupAddress')}")
    print(f"Delivery address: {result.get('deliveryAddress')}")
    print(f"Zapisano wynik TO details: {result_path}")
    print(f"Zapisano zrzut TO details: {screenshot_path}")

    clicked_vehicle = driver.execute_script(CLICK_TAB_SCRIPT, "Vehicle details")
    print(f"Klik Vehicle details: {yes_no(clicked_vehicle)}")
    time.sleep(1.2)

    vehicle_result = driver.execute_script(DETAILS_SCRIPT)
    vehicle_path = OUT_DIR / "details_vehicle_details_result.json"
    vehicle_screenshot = OUT_DIR / "details_vehicle_details_screenshot.png"

    vehicle_path.write_text(json.dumps(vehicle_result, ensure_ascii=False, indent=2), encoding="utf-8")
    driver.save_screenshot(str(vehicle_screenshot))

    print("Wynik Vehicle details:")
    print(f"Vehicle type: {vehicle_result.get('vehicleType') or value_from_sample(vehicle_result.get('sample', ''), ['Vehicle type:', 'Vehicle type'])}")
    print(f"Zapisano wynik Vehicle details: {vehicle_path}")
    print(f"Zapisano zrzut Vehicle details: {vehicle_screenshot}")


def value_from_sample(text, labels):
    lines = [line.strip() for line in (text or "").splitlines() if line.strip()]
    for i, line in enumerate(lines):
        for label in labels:
            if line == label and i + 1 < len(lines):
                return lines[i + 1]
            if line.startswith(label):
                value = line[len(label):].strip()
                if value:
                    return value
    return ""


if __name__ == "__main__":
    main()
