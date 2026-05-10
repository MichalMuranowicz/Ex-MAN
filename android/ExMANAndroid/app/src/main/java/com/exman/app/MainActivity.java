package com.exman.app;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class MainActivity extends Activity {
    private static final String LOGIN_URL = "https://motos.man.eu/motos/frame/frame_en.html";
    private static final boolean ENABLE_WEBVIEW_DEBUG = false;
    private static final int MODE_IDLE = 0;
    private static final int MODE_WAIT_DETAILS_PAGE = 1;
    private static final int MAX_LIST_PAGES = 20;
    private static final int REQUEST_EXPORT_XLSX = 2001;
    private static final String[] COLUMNS = new String[] {
            "NAZWA / SKROT", "NUMER TELEFONU", "STAWKA", "FAKTURA GOTOWKA",
            "ZALADUNEK PLANOWANA DATA", "ZALADUNEK KRAJ", "ZALADUNEK KOD POCZTOWY",
            "ZALADUNEK MIASTO", "ZALADUNEK ULICA NUMER",
            "DOSTAWA PLANOWANA DATA", "DOSTAWA KRAJ", "DOSTAWA KOD POCZTOWY",
            "DOSTAWA MIASTO", "DOSTAWA ULICA NUMER",
            "VIN NUMER REFERENCYJNY", "TOWAR NAZWA URZADZENIA",
            "DLUGOSC [mm]", "SZEROKOSC [mm]", "WYSOKOSC [mm]",
            "ROZSTAW OSI [mm]", "WAGA [kg]",
            "BOOKING PRZESYLKA TMS", "WYSZUKIWANIE",
            "KIEROWCA ZALADUNEK", "FORMATKA DLA KIEROWCY"
    };

    private WebView webView;
    private TextView statusText;
    private ProgressBar progressBar;
    private Button scrapeButton;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private int scrapeMode = MODE_IDLE;
    private JSONArray scrapeUnits = new JSONArray();
    private JSONArray scrapedRows = new JSONArray();
    private int scrapeIndex = 0;
    private int listPageNumber = 1;
    private JSONObject currentUnit;

    @Override
    @SuppressLint("SetJavaScriptEnabled")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Ex MAN");

        WebView.setWebContentsDebuggingEnabled(ENABLE_WEBVIEW_DEBUG);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.rgb(16, 20, 24));
        setContentView(root);

        ImageView banner = new ImageView(this);
        banner.setImageResource(getResources().getIdentifier("man_motos_excel_banner", "drawable", getPackageName()));
        banner.setAdjustViewBounds(true);
        banner.setScaleType(ImageView.ScaleType.FIT_CENTER);
        root.addView(banner, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(96)
        ));

        LinearLayout toolbar = new LinearLayout(this);
        toolbar.setOrientation(LinearLayout.HORIZONTAL);
        toolbar.setGravity(Gravity.CENTER_VERTICAL);
        toolbar.setPadding(dp(10), dp(6), dp(10), dp(6));
        root.addView(toolbar, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        scrapeButton = makeButton("Pobierz i zapisz");
        scrapeButton.setOnClickListener(v -> startScrape());
        toolbar.addView(scrapeButton, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(42)
        ));

        progressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        progressBar.setMax(100);

        webView = new WebView(this);
        configureWebView(webView);
        root.addView(webView, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1
        ));

        root.addView(progressBar, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(5)
        ));

        statusText = new TextView(this);
        statusText.setTextColor(Color.rgb(24, 33, 43));
        statusText.setTextSize(12);
        statusText.setSingleLine(true);
        statusText.setGravity(Gravity.CENTER_VERTICAL);
        statusText.setBackgroundColor(Color.rgb(244, 247, 250));
        statusText.setPadding(dp(10), 0, dp(10), 0);
        root.addView(statusText, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(30)
        ));

        setStatus("Zaloguj sie, potem kliknij Pobierz i zapisz");
        progressBar.setProgress(0);
        webView.loadUrl(LOGIN_URL);
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void configureWebView(WebView view) {
        WebSettings settings = view.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        settings.setBuiltInZoomControls(true);
        settings.setDisplayZoomControls(false);
        settings.setSupportMultipleWindows(false);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE);
        settings.setUserAgentString(settings.getUserAgentString() + " ExMANAndroid/0.1");

        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        cookieManager.setAcceptThirdPartyCookies(view, true);

        view.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (scrapeButton != null && scrapeButton.isEnabled()) {
                    progressBar.setProgress(newProgress);
                }
            }
        });

        view.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                if (scrapeMode == MODE_IDLE && scrapeButton.isEnabled()) {
                    setStatus("Gotowe do pobierania");
                }
                if (scrapeMode == MODE_WAIT_DETAILS_PAGE) {
                    scrapeMode = MODE_IDLE;
                    handler.postDelayed(() -> scrapeCurrentDetails(), 600);
                }
            }
        });
    }

    private Button makeButton(String text) {
        Button button = new Button(this);
        button.setText(text);
        button.setAllCaps(false);
        button.setTextColor(Color.WHITE);
        button.setBackgroundColor(Color.rgb(232, 81, 26));
        return button;
    }

    private String readAsset(String name) {
        try (InputStream input = getAssets().open(name);
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[4096];
            int read;
            while ((read = input.read(buffer)) != -1) {
                output.write(buffer, 0, read);
            }
            return output.toString(StandardCharsets.UTF_8.name());
        } catch (Exception e) {
            setProgress(0, "Nie moge wczytac pliku aplikacji");
            return "";
        }
    }

    private JSONObject parseEvalJson(String value) throws Exception {
        Object decoded = new JSONTokener(value).nextValue();
        String rawJson = decoded instanceof String ? (String) decoded : String.valueOf(decoded);
        return new JSONObject(rawJson);
    }

    private void startScrape() {
        setProgress(0, "Szukam jednostek kod 20...");
        scrapeButton.setEnabled(false);
        scrapedRows = new JSONArray();
        scrapeUnits = new JSONArray();
        scrapeIndex = 0;
        listPageNumber = 1;
        currentUnit = null;

        collectCurrentListPage();
    }

    private void collectCurrentListPage() {
        webView.evaluateJavascript(readAsset("list_units.js"), value -> {
            try {
                JSONObject result = parseEvalJson(value);
                JSONArray pageUnits = result.optJSONArray("units");
                int newCount = appendNewUnits(pageUnits);

                setProgress(0, "Lista: strona " + listPageNumber + ", znaleziono " + scrapeUnits.length());

                if (listPageNumber > 1 && newCount == 0) {
                    processAfterListCollection();
                    return;
                }

                if (listPageNumber >= MAX_LIST_PAGES) {
                    processAfterListCollection();
                    return;
                }

                int nextPage = listPageNumber + 1;
                clickPage(nextPage, clicked -> {
                    if (!clicked) {
                        processAfterListCollection();
                        return;
                    }
                    listPageNumber = nextPage;
                    setProgress(0, "Przechodze do strony listy " + listPageNumber + "...");
                    handler.postDelayed(() -> collectCurrentListPage(), 2500);
                });
            } catch (Exception e) {
                setProgress(0, "Blad odczytu listy");
                scrapeButton.setEnabled(true);
            }
        });
    }

    private void processAfterListCollection() {
        try {
            setProgress(0, "Znaleziono " + scrapeUnits.length() + " jednostek");
                if (scrapeUnits.length() == 0) {
                    setProgress(0, "Brak jednostek. Przejdz do listy transportow");
                    scrapeButton.setEnabled(true);
                    return;
                }

                processNextUnit();
        } catch (Exception e) {
            setProgress(0, "Blad przygotowania listy");
            scrapeButton.setEnabled(true);
        }
    }

    private int appendNewUnits(JSONArray pageUnits) {
        if (pageUnits == null) return 0;
        int newCount = 0;
        for (int i = 0; i < pageUnits.length(); i++) {
            JSONObject unit = pageUnits.optJSONObject(i);
            if (unit == null) continue;
            String href = unit.optString("href");
            if (href.isEmpty() || unitExists(href)) continue;
            scrapeUnits.put(unit);
            newCount++;
        }
        return newCount;
    }

    private boolean unitExists(String href) {
        for (int i = 0; i < scrapeUnits.length(); i++) {
            JSONObject unit = scrapeUnits.optJSONObject(i);
            if (unit != null && href.equals(unit.optString("href"))) {
                return true;
            }
        }
        return false;
    }

    private void processNextUnit() {
        if (scrapeIndex >= scrapeUnits.length()) {
            setProgress(100, "Pobrano " + scrapedRows.length() + "/" + scrapeUnits.length() + ". Wybierz zapis pliku");
            scrapeButton.setEnabled(true);
            if (scrapedRows.length() > 0) {
                promptExportXlsx();
            }
            return;
        }

        currentUnit = scrapeUnits.optJSONObject(scrapeIndex);
        if (currentUnit == null || currentUnit.optString("href").isEmpty()) {
            scrapeIndex++;
            processNextUnit();
            return;
        }

        updateDownloadProgress("Pobieram " + (scrapeIndex + 1) + "/" + scrapeUnits.length()
                + ": " + currentUnit.optString("vehicleNo"));
        scrapeMode = MODE_WAIT_DETAILS_PAGE;
        webView.loadUrl(currentUnit.optString("href"));
    }

    private void scrapeCurrentDetails() {
        clickTab("TO details", clicked -> handler.postDelayed(() -> {
            webView.evaluateJavascript(readAsset("details_extract.js"), value -> {
                try {
                    JSONObject details = parseEvalJson(value);
                    JSONObject pickup = details.optJSONObject("pickupAddress");
                    String pickupStreet = pickup == null ? "" : pickup.optString("street");

                    if (isBialezyce100(pickupStreet)) {
                        clickTab("Vehicle details", vehicleClicked -> handler.postDelayed(() -> {
                            webView.evaluateJavascript(readAsset("details_extract.js"), vehicleValue -> {
                                try {
                                    JSONObject vehicleDetails = parseEvalJson(vehicleValue);
                                    addScrapedRow(details, vehicleDetails.optString("vehicleType"));
                                } catch (Exception e) {
                                    addScrapedRow(details, "");
                                }
                                scrapeIndex++;
                                processNextUnit();
                            });
                        }, 500));
                    } else {
                        addScrapedRow(details, "");
                        scrapeIndex++;
                        processNextUnit();
                    }
                } catch (Exception e) {
                    updateDownloadProgress("Blad rekordu " + (scrapeIndex + 1) + "/" + scrapeUnits.length());
                    scrapeIndex++;
                    processNextUnit();
                }
            });
        }, 700));
    }

    private interface BoolCallback {
        void call(boolean value);
    }

    private void clickTab(String label, BoolCallback callback) {
        String escaped = label.replace("\\", "\\\\").replace("\"", "\\\"");
        String script = readAsset("click_tab.js").replace("__LABEL__", escaped);
        webView.evaluateJavascript(script, value -> callback.call("true".equals(value)));
    }

    private void clickPage(int pageNumber, BoolCallback callback) {
        String script = readAsset("click_tab.js").replace("__LABEL__", String.valueOf(pageNumber));
        webView.evaluateJavascript(script, value -> callback.call("true".equals(value)));
    }

    private void addScrapedRow(JSONObject details, String vehicleType) {
        JSONObject pickup = details.optJSONObject("pickupAddress");
        JSONObject delivery = details.optJSONObject("deliveryAddress");
        if (pickup == null) pickup = new JSONObject();
        if (delivery == null) delivery = new JSONObject();

        String product = appendAfterComma(details.optString("typeCab"), vehicleType);

        JSONObject row = new JSONObject();
        try {
            row.put("NAZWA / SKROT", "MAN");
            row.put("NUMER TELEFONU", details.optString("telephone"));
            row.put("ZALADUNEK PLANOWANA DATA", details.optString("pickupUntil"));
            row.put("ZALADUNEK KRAJ", pickup.optString("country"));
            row.put("ZALADUNEK KOD POCZTOWY", pickup.optString("zipcode"));
            row.put("ZALADUNEK MIASTO", pickup.optString("city"));
            row.put("ZALADUNEK ULICA NUMER", pickup.optString("street"));
            row.put("DOSTAWA PLANOWANA DATA", details.optString("latestDelivery"));
            row.put("DOSTAWA KRAJ", delivery.optString("country"));
            row.put("DOSTAWA KOD POCZTOWY", delivery.optString("zipcode"));
            row.put("DOSTAWA MIASTO", delivery.optString("city"));
            row.put("DOSTAWA ULICA NUMER", delivery.optString("street"));
            row.put("VIN NUMER REFERENCYJNY", currentUnit == null ? "" : currentUnit.optString("vehicleNo"));
            row.put("TOWAR NAZWA URZADZENIA", product);
            row.put("DLUGOSC [mm]", details.optString("totalLength"));
            row.put("WYSOKOSC [mm]", details.optString("height"));
            row.put("WAGA [kg]", details.optString("weight"));
            row.put("BOOKING PRZESYLKA TMS", currentUnit == null ? "" : currentUnit.optString("toNo"));
            row.put("WYSZUKIWANIE", details.optString("vin"));
            scrapedRows.put(row);

            updateDownloadProgress("Pobrano " + scrapedRows.length() + "/" + scrapeUnits.length()
                    + ": " + row.optString("VIN NUMER REFERENCYJNY"));
        } catch (Exception e) {
            updateDownloadProgress("Nie udalo sie dodac wiersza");
        }
    }

    private boolean isBialezyce100(String street) {
        return street != null && street.trim().replaceAll("\\s+", " ").equalsIgnoreCase("Bialezyce 100");
    }

    private String appendAfterComma(String baseValue, String extraValue) {
        String base = baseValue == null ? "" : baseValue.trim();
        String extra = extraValue == null ? "" : extraValue.trim();
        if (extra.isEmpty()) return base;
        if (base.isEmpty()) return extra;
        if (base.contains(extra)) return base;
        return base + ", " + extra;
    }

    private void promptExportXlsx() {
        if (scrapedRows.length() == 0) {
            setProgress(0, "Brak danych do zapisania");
            return;
        }

        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        intent.putExtra(Intent.EXTRA_TITLE, "MAN extract.xlsx");
        startActivityForResult(intent, REQUEST_EXPORT_XLSX);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != REQUEST_EXPORT_XLSX) return;

        if (resultCode != RESULT_OK || data == null || data.getData() == null) {
            setProgress(100, "Zapis anulowany");
            return;
        }

        Uri uri = data.getData();
        try (OutputStream output = getContentResolver().openOutputStream(uri, "wt")) {
            if (output == null) {
                setProgress(100, "Nie moge otworzyc pliku do zapisu");
                return;
            }
            writeXlsx(output);
            setProgress(100, "Zapisano MAN extract.xlsx");
            handler.postDelayed(() -> returnToLoginScreen(), 1200);
        } catch (Exception e) {
            setProgress(100, "Blad zapisu XLSX");
        }
    }

    private void returnToLoginScreen() {
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.removeAllCookies(value -> {
            cookieManager.flush();
            webView.clearCache(true);
            webView.clearHistory();
            scrapedRows = new JSONArray();
            scrapeUnits = new JSONArray();
            scrapeIndex = 0;
            currentUnit = null;
            scrapeButton.setEnabled(true);
            setProgress(0, "Zaloguj sie, potem kliknij Pobierz i zapisz");
            webView.loadUrl(LOGIN_URL);
        });
    }

    private void writeXlsx(OutputStream output) throws Exception {
        ZipOutputStream zip = new ZipOutputStream(output);
        addZipEntry(zip, "[Content_Types].xml",
                "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
                "<Types xmlns=\"http://schemas.openxmlformats.org/package/2006/content-types\">" +
                "<Default Extension=\"rels\" ContentType=\"application/vnd.openxmlformats-package.relationships+xml\"/>" +
                "<Default Extension=\"xml\" ContentType=\"application/xml\"/>" +
                "<Override PartName=\"/xl/workbook.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml\"/>" +
                "<Override PartName=\"/xl/worksheets/sheet1.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml\"/>" +
                "<Override PartName=\"/xl/styles.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml\"/>" +
                "</Types>");
        addZipEntry(zip, "_rels/.rels",
                "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
                "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">" +
                "<Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument\" Target=\"xl/workbook.xml\"/>" +
                "</Relationships>");
        addZipEntry(zip, "xl/_rels/workbook.xml.rels",
                "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
                "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">" +
                "<Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet\" Target=\"worksheets/sheet1.xml\"/>" +
                "<Relationship Id=\"rId2\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles\" Target=\"styles.xml\"/>" +
                "</Relationships>");
        addZipEntry(zip, "xl/workbook.xml",
                "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
                "<workbook xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\" " +
                "xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\">" +
                "<sheets><sheet name=\"Jednostki kod 20\" sheetId=\"1\" r:id=\"rId1\"/></sheets>" +
                "</workbook>");
        addZipEntry(zip, "xl/styles.xml", buildStylesXml());
        addZipEntry(zip, "xl/worksheets/sheet1.xml", buildSheetXml());
        zip.finish();
    }

    private void addZipEntry(ZipOutputStream zip, String name, String content) throws Exception {
        zip.putNextEntry(new ZipEntry(name));
        zip.write(content.getBytes(StandardCharsets.UTF_8));
        zip.closeEntry();
    }

    private String buildStylesXml() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
                "<styleSheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\">" +
                "<fonts count=\"2\"><font><sz val=\"11\"/><name val=\"Calibri\"/></font>" +
                "<font><b/><sz val=\"10\"/><color rgb=\"FFFFFFFF\"/><name val=\"Calibri\"/></font></fonts>" +
                "<fills count=\"3\"><fill><patternFill patternType=\"none\"/></fill>" +
                "<fill><patternFill patternType=\"gray125\"/></fill>" +
                "<fill><patternFill patternType=\"solid\"><fgColor rgb=\"FF1C2B4A\"/><bgColor indexed=\"64\"/></patternFill></fill></fills>" +
                "<borders count=\"1\"><border><left/><right/><top/><bottom/><diagonal/></border></borders>" +
                "<cellStyleXfs count=\"1\"><xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"0\"/></cellStyleXfs>" +
                "<cellXfs count=\"2\"><xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"0\" xfId=\"0\" applyAlignment=\"1\">" +
                "<alignment vertical=\"center\" wrapText=\"1\"/></xf>" +
                "<xf numFmtId=\"0\" fontId=\"1\" fillId=\"2\" borderId=\"0\" xfId=\"0\" applyFont=\"1\" applyFill=\"1\" applyAlignment=\"1\">" +
                "<alignment horizontal=\"center\" vertical=\"center\" wrapText=\"1\"/></xf></cellXfs>" +
                "<cellStyles count=\"1\"><cellStyle name=\"Normal\" xfId=\"0\" builtinId=\"0\"/></cellStyles>" +
                "</styleSheet>";
    }

    private String buildSheetXml() {
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>");
        xml.append("<worksheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\">");
        xml.append("<sheetViews><sheetView workbookViewId=\"0\"><pane ySplit=\"1\" topLeftCell=\"A2\" activePane=\"bottomLeft\" state=\"frozen\"/></sheetView></sheetViews>");
        xml.append("<cols>");
        for (int i = 1; i <= COLUMNS.length; i++) {
            xml.append("<col min=\"").append(i).append("\" max=\"").append(i).append("\" width=\"22\" customWidth=\"1\"/>");
        }
        xml.append("</cols>");
        xml.append("<sheetData>");
        xml.append("<row r=\"1\" ht=\"35\" customHeight=\"1\">");
        for (int c = 0; c < COLUMNS.length; c++) {
            xml.append(cellXml(1, c + 1, COLUMNS[c], 1));
        }
        xml.append("</row>");

        for (int r = 0; r < scrapedRows.length(); r++) {
            JSONObject row = scrapedRows.optJSONObject(r);
            int excelRow = r + 2;
            xml.append("<row r=\"").append(excelRow).append("\" ht=\"20\" customHeight=\"1\">");
            for (int c = 0; c < COLUMNS.length; c++) {
                String value = row == null ? "" : row.optString(COLUMNS[c]);
                xml.append(cellXml(excelRow, c + 1, value, 0));
            }
            xml.append("</row>");
        }

        xml.append("</sheetData>");
        xml.append("</worksheet>");
        return xml.toString();
    }

    private String cellXml(int row, int col, String value, int style) {
        String ref = columnName(col) + row;
        String safeValue = xmlEscape(value == null ? "" : value);
        return "<c r=\"" + ref + "\" t=\"inlineStr\" s=\"" + style + "\"><is><t>" + safeValue + "</t></is></c>";
    }

    private String columnName(int column) {
        StringBuilder name = new StringBuilder();
        int value = column;
        while (value > 0) {
            int mod = (value - 1) % 26;
            name.insert(0, (char) ('A' + mod));
            value = (value - mod - 1) / 26;
        }
        return name.toString();
    }

    private String xmlEscape(String value) {
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }

    private void inspectCurrentPage() {
        appendStatus("Sprawdzam, co widzi WebView...");

        String script =
                "(function() {" +
                "  function collect(win, path, out) {" +
                "    try {" +
                "      const doc = win.document;" +
                "      const bodyText = (doc.body && doc.body.innerText) ? doc.body.innerText : '';" +
                "      const rows = Array.from(doc.querySelectorAll('tr')).map(function(row) { return row.innerText || ''; });" +
                "      out.frames.push({path:path, url:win.location.href, title:doc.title || '', text:bodyText, rows:rows});" +
                "      for (let i = 0; i < win.frames.length; i++) collect(win.frames[i], path + '/frame[' + i + ']', out);" +
                "    } catch(e) {" +
                "      out.frames.push({path:path, error:String(e), text:'', rows:[]});" +
                "    }" +
                "  }" +
                "  const out = {frames:[]};" +
                "  collect(window, 'top', out);" +
                "  const bodyText = out.frames.map(function(frame) { return frame.text || ''; }).join('\\n');" +
                "  const rows = [];" +
                "  out.frames.forEach(function(frame) {" +
                "    (frame.rows || []).forEach(function(rowText) { rows.push({frame:frame.path, text:rowText}); });" +
                "  });" +
                "  const code20Rows = rows.filter(function(row) {" +
                "    const text = row.text || '';" +
                "    return /(^|\\s)20(\\s|$)/.test(text);" +
                "  });" +
                "  const units = [];" +
                "  function collectUnits(win, path) {" +
                "    try {" +
                "      const doc = win.document;" +
                "      Array.from(doc.querySelectorAll('tr')).forEach(function(row) {" +
                "        const cells = Array.from(row.querySelectorAll('td')).map(function(cell) { return (cell.innerText || '').trim(); });" +
                "        const nonEmpty = cells.filter(Boolean);" +
                "        if (nonEmpty[0] !== '20') return;" +
                "        if (!/^[A-Z0-9]{6,8}$/.test(nonEmpty[1] || '')) return;" +
                "        const link = row.querySelector(\"a[href*='transportDetails'], a[href*='transport']\");" +
                "        units.push({frame:path, vehicleNo:nonEmpty[1] || '', pickupCode:nonEmpty[2] || '', pickupDate:nonEmpty[3] || '', country:nonEmpty[4] || '', dest:nonEmpty[5] || '', toNo:nonEmpty[16] || '', href:link ? link.href : '', cells:nonEmpty});" +
                "      });" +
                "      for (let i = 0; i < win.frames.length; i++) collectUnits(win.frames[i], path + '/frame[' + i + ']');" +
                "    } catch(e) {}" +
                "  }" +
                "  collectUnits(window, 'top');" +
                "  return JSON.stringify({" +
                "    url: location.href," +
                "    title: document.title," +
                "    hasVehicNo: bodyText.indexOf('Vehic. no') !== -1," +
                "    hasToDetails: bodyText.indexOf('TO details') !== -1," +
                "    hasPickup: bodyText.indexOf('Pick up') !== -1," +
                "    frames: out.frames.map(function(frame) {" +
                "      return {path:frame.path, url:frame.url || '', title:frame.title || '', textLength:(frame.text || '').length, rows:(frame.rows || []).length, error:frame.error || ''};" +
                "    })," +
                "    rows: rows.length," +
                "    code20Rows: code20Rows.length," +
                "    firstCode20Rows: code20Rows.slice(0, 5)," +
                "    units: units," +
                "    firstUnits: units.slice(0, 10)," +
                "    sample: bodyText.slice(0, 700)" +
                "  });" +
                "})();";

        webView.evaluateJavascript(script, value -> {
            try {
                Object decoded = new JSONTokener(value).nextValue();
                String rawJson = decoded instanceof String ? (String) decoded : String.valueOf(decoded);
                JSONObject result = new JSONObject(rawJson);

                appendStatus("URL: " + result.optString("url"));
                appendStatus("Tytul: " + result.optString("title"));
                appendStatus("Widze 'Vehic. no': " + yesNo(result.optBoolean("hasVehicNo")));
                appendStatus("Widze 'TO details': " + yesNo(result.optBoolean("hasToDetails")));
                appendStatus("Widze 'Pick up': " + yesNo(result.optBoolean("hasPickup")));
                appendStatus("Liczba wierszy HTML: " + result.optInt("rows"));
                appendStatus("Podejrzane wiersze z kodem 20: " + result.optInt("code20Rows"));
                JSONArray inspectedUnits = result.optJSONArray("units");
                appendStatus("Rekordy kod 20: " + (inspectedUnits == null ? 0 : inspectedUnits.length()));
                appendStatus("Pierwsze rekordy: " + result.optJSONArray("firstUnits"));
                appendStatus("Ramki: " + result.optJSONArray("frames"));
                appendStatus("Probka tekstu: " + result.optString("sample").replace("\n", " | "));
            } catch (Exception e) {
                appendStatus("Nie udalo sie odczytac strony: " + e.getMessage());
                appendStatus("Surowy wynik: " + value);
            }
        });
    }

    private String yesNo(boolean value) {
        return value ? "TAK" : "NIE";
    }

    private void appendStatus(String text) {
        setStatus(text);
    }

    private void setStatus(String text) {
        runOnUiThread(() -> statusText.setText(text));
    }

    private void setProgress(int percent, String text) {
        runOnUiThread(() -> {
            progressBar.setProgress(Math.max(0, Math.min(100, percent)));
            statusText.setText(text);
        });
    }

    private void updateDownloadProgress(String text) {
        int total = scrapeUnits.length();
        int done = scrapedRows.length();
        int percent = total <= 0 ? 0 : Math.round((done * 100f) / total);
        setProgress(percent, text);
    }

    private int dp(int value) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(value * density);
    }

    @Override
    public void onBackPressed() {
        if (webView != null && webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
