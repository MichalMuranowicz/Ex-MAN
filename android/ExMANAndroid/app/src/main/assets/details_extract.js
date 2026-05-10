(function() {
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

  const out = {frames: []};
  collect(window, 'top', out);
  const text = out.frames.map(function(frame) { return frame.text || ''; }).join('\n');
  const addresses = extractAddresses(text);

  return JSON.stringify({
    url: location.href,
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
    sample: text.slice(0, 1000)
  });
})();
