# Ex-MAN

Cross-platform logistics automation tool for extracting structured transport data from a web-based logistics workflow and exporting it to Excel.

Ex-MAN contains a Windows desktop application and an Android application built around the same practical logistics workflow: collect selected records from a web portal, map them into structured logistics fields and export the result as an Excel workbook.

## Why This Project Matters

Logistics work often involves repetitive data collection from web-based operational systems. Ex-MAN reduces that manual effort by turning portal data into a structured Excel workbook that can be reviewed, shared or imported into downstream processes.

The project focuses on practical value: clear data mapping, predictable export columns, a simple user interface and a workflow designed around real transport data collection needs.

## Project Overview

This repository contains:

- Windows desktop version
- Android version
- Excel export workflow
- Web-based data extraction process

The desktop and mobile versions follow the same business goal but use different platform-specific implementations. The Windows app uses browser automation, while the Android app uses a WebView workflow.

## Key Features

- Desktop automation workflow for collecting selected transport records
- Android WebView workflow for mobile use
- Structured Excel export to `MAN extract.xlsx`
- Logistic data mapping for pickup, delivery, dates, VIN/reference values and vehicle details
- Basic error handling for missing data, empty results and locked Excel output files
- User-friendly interface with compact progress/status feedback
- Secure credential handling notes for local usage and repository hygiene

## Technologies Used

### Desktop

- Python
- Selenium
- customtkinter
- openpyxl
- webdriver-manager
- Pillow
- PyInstaller

### Android

- Java
- Android SDK
- WebView
- XLSX export workflow

## My Role

I designed the business workflow, defined the data mapping rules, tested the extraction process, prepared the desktop and Android usage scenarios, reviewed AI-assisted code suggestions and organized the repository as a portfolio project.

The project combines my logistics background with practical automation and AI-assisted software development.

## Security Notes

- Do not store credentials in the repository.
- Do not commit exported Excel files.
- Do not commit APK, EXE, keystore or private build artifacts directly to the repository.
- Windows credentials, if saved, should rely on secure local mechanisms.
- Android version should not store portal credentials locally.
- Repository should contain no real user data, company data or session cookies.

More details are available in [docs/security.md](docs/security.md).

## Repository Structure

```text
Ex-MAN/
  README.md
  README_EXE.txt
  requirements.txt
  build_exe.bat
  Ex MAN.spec
  man_extractor.py
  assets/
    ex_man.ico
    ex_man_icon_256.png
    man_motos_excel_banner.png
    man_motos_excel_banner.svg
  android/
    ExMANAndroid/
      README_ANDROID.md
      build.gradle
      settings.gradle
      build_android_debug.bat
      install_android_debug.bat
      run_desktop_webview_test.bat
      desktop_webview_test.py
      app/
        build.gradle
        src/main/
          AndroidManifest.xml
          assets/
            click_tab.js
            details_extract.js
            list_units.js
          java/com/exman/app/
            MainActivity.java
          res/
            drawable/
            values/
  docs/
    architecture.md
    workflow.md
    security.md
    roadmap.md
    github-profile-note.md
```

Generated builds, local SDK files, caches and private signing artifacts are intentionally excluded from the repository.

## Screenshots

Screenshots will be added in the next documentation update.

Planned screenshot paths:

- `docs/screenshots/windows-main.png`
- `docs/screenshots/windows-export.png`
- `docs/screenshots/android-main.png`
- `docs/screenshots/excel-output.png`

## How to Run - Desktop

1. Install Python on Windows.
2. Install Google Chrome.
3. Install Python dependencies:

```bat
py -m pip install -r requirements.txt
```

4. Run the desktop application:

```bat
py man_extractor.py
```

5. Log in through the application UI, choose an export folder and start the extraction workflow.

To build a Windows executable:

```bat
build_exe.bat
```

The generated EXE should be distributed through GitHub Releases, not committed directly to the repository.

## How to Build - Android

1. Open `android/ExMANAndroid` in Android Studio, or use the included helper scripts.
2. Make sure Android SDK and build tools are installed.
3. Build a debug APK:

```bat
android\ExMANAndroid\build_android_debug.bat
```

4. Install on a connected test device:

```bat
android\ExMANAndroid\install_android_debug.bat
```

Release APKs, AAB files and signing keys should not be committed to the repository. Publish release builds through GitHub Releases when needed.

## Documentation

- [Architecture](docs/architecture.md)
- [Workflow](docs/workflow.md)
- [Security](docs/security.md)
- [Roadmap](docs/roadmap.md)
- [GitHub profile note](docs/github-profile-note.md)

## Roadmap

- Add screenshots to the README
- Publish APK and Windows build through GitHub Releases
- Add anonymized sample Excel output
- Split the Python desktop application into smaller modules
- Add logging improvements
- Add user-friendly error messages
- Add basic automated tests for data mapping
- Improve Android UI polish
- Add a configuration file example without sensitive data

See [docs/roadmap.md](docs/roadmap.md) for the full roadmap.

## License

No license has been selected yet. Until a license is added, all rights are reserved by the repository owner.

## Portfolio Note

This project demonstrates practical process automation, logistics domain knowledge and AI-assisted software development. It shows how a repetitive operational workflow can be turned into a focused desktop and mobile tool with structured Excel output.

