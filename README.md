# Ex MAN

![Ex MAN banner](assets/man_motos_excel_banner.png)

Ex MAN is a two-platform automation project for extracting logistics data from the MAN MOTOS portal and exporting it to an Excel workbook.

The project contains:

- a Windows desktop app built with Python, customtkinter, Selenium and openpyxl,
- a native Android app built in Java with WebView,
- shared business rules for filtering records with code `20`,
- Excel export to `MAN extract.xlsx`,
- custom branding assets for the app icon and banner.

The repository is intended as a portfolio project showing practical automation, desktop UI, mobile WebView automation, data mapping and XLSX generation.

## Business Goal

The application helps collect selected transport/order data from MAN MOTOS:

1. log in to the MAN MOTOS portal,
2. find units with code `20`,
3. open each matching record,
4. read logistics and vehicle details,
5. map source fields to the agreed Excel columns,
6. save the result as `MAN extract.xlsx`.

The most important fields are VIN/reference numbers, pickup data, delivery data, dates and basic vehicle parameters.

## Platforms

### Windows Desktop

Location:

`man_extractor.py`

Technology:

- Python
- customtkinter
- Selenium
- Chrome / ChromeDriver
- openpyxl
- PyInstaller

Main features:

- desktop UI named `Ex MAN`,
- login and password fields,
- optional local credential saving on Windows,
- compact progress/status area instead of a large technical log box,
- fast DOM reading with JavaScript executed through Selenium,
- Excel export with fixed columns,
- handling for locked Excel files.

Current user-facing version in code:

`Ex MAN v1.1`

### Android

Location:

`android/ExMANAndroid`

Technology:

- native Android app
- Java
- WebView
- manually generated XLSX file
- signed APK distribution

Main features:

- app name `Ex MAN`,
- MAN MOTOS login page opens immediately,
- simplified interface with one main button: `Pobierz i zapisz`,
- WebView-based portal access,
- compact progress bar and status text,
- export through the Android document picker,
- return to the login screen after saving,
- no local password saving on Android.

Minimum supported Android version:

`Android 8.0`, `minSdk 26`

## Data Mapping

The final mapping rules are:

| Source | Excel column |
| --- | --- |
| Constant value `MAN` | `NAZWA / SKROT` |
| Main list `Vehic. no` | `VIN NUMER REFERENCYJNY` |
| TO details `VIN` | `WYSZUKIWANIE` |
| TO details `Type / cab` | `TOWAR NAZWA URZADZENIA` |
| TO details `Total length` | `DLUGOSC [mm]` |
| TO details `Height` | `WYSOKOSC [mm]` |
| TO details `Weight` | `WAGA [kg]` |
| TO details `Pick up until` | `ZALADUNEK PLANOWANA DATA` |
| TO details `Latest delivery` | `DOSTAWA PLANOWANA DATA` |
| TO details `Telephone number` | `NUMER TELEFONU` |
| TO details `Pick up` address | pickup country, postal code, city and street columns |
| TO details `Delivery` address | delivery country, postal code, city and street columns |
| Vehicle details `Vehicle type` | appended to product name only for pickup street `Bialezyce 100` |

Important distinction:

- `Vehic. no` from the list is treated as the reference number,
- `VIN` from TO details is treated as the search value.

These two fields should not be swapped.

## Repository Structure

```text
MAN_Ekstraktor/
  man_extractor.py                         Windows desktop application
  build_exe.bat                            Windows EXE build helper
  Ex MAN.spec                              PyInstaller spec for the current EXE name
  requirements.txt                         Python dependencies
  README.md                                Main GitHub portfolio README
  README_EXE.txt                           Short Windows distribution notes
  assets/                                  Shared app icon and banner assets
  dist/                                    Generated Windows EXE artifacts
  build/                                   PyInstaller build output
  android/
    ExMANAndroid/
      app/src/main/java/com/exman/app/
        MainActivity.java                  Android app logic
      app/src/main/assets/
        list_units.js                      list scraping script
        details_extract.js                 detail scraping script
        click_tab.js                       WebView click helper
      app/src/main/res/                    Android resources
      release/                             signed APK artifacts and signing files
```

## Build Notes

### Windows EXE

Build helper:

```bat
build_exe.bat
```

Expected final executable:

```text
dist\Ex MAN.exe
```

The target computer needs Google Chrome. Python is not required for the final one-file EXE.

### Android APK

Android project:

```text
android\ExMANAndroid
```

Debug build helper:

```bat
build_android_debug.bat
```

Install helper:

```bat
install_android_debug.bat
```

Release artifacts are currently stored under:

```text
android\ExMANAndroid\release
```

## Security Notes

- The project does not contain MAN MOTOS credentials.
- The Windows app can remember credentials only when the user explicitly enables it.
- Windows saved credentials are protected with Windows DPAPI for the current user profile.
- The Android app does not store the login or password locally as a project assumption.
- The Android app communicates with MAN MOTOS through WebView and writes only the selected XLSX export.
- This is a practical architecture review, not a formal security audit.

## GitHub Portfolio Notes

Before publishing this repository publicly, review generated and private files carefully:

- do not publish private credentials,
- do not publish production signing keys such as `.jks` files,
- consider moving generated EXE/APK files to GitHub Releases instead of committing them,
- consider excluding local SDK/build folders such as `.android-sdk`, `.gradle`, `build`, `dist` and Android build outputs,
- keep screenshots and branding assets that help present the project.

Suggested portfolio angle:

- desktop automation with Selenium and JavaScript DOM extraction,
- native Android WebView automation,
- cross-platform implementation of the same business workflow,
- Excel generation and fixed-column data mapping,
- practical UI simplification based on user feedback,
- performance optimization from slow selector-heavy scraping to faster DOM-based extraction.

## Limitations

- MAN MOTOS access requires valid user credentials and network access.
- The portal layout is external and can change, which may require updates to selectors or DOM parsing logic.
- Not every Excel column is always populated; priority is correctness of key logistics fields.
- Android export behavior depends on the system document picker and may create names such as `MAN extract (1).xlsx`.

