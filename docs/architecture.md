# Architecture

Ex-MAN is organized as a portfolio repository containing two platform-specific applications that support the same logistics automation workflow.

The project is intentionally structured around practical business use rather than a framework showcase. The core idea is to extract selected data from a web-based logistics workflow, map it into predictable fields and export it to Excel.

## High-Level Components

```text
Web-based logistics workflow
        |
        | selected records and details
        v
Extraction process
        |
        | mapped logistics fields
        v
Excel export workflow
```

## Desktop Application

The Windows desktop application is implemented in Python.

Main responsibilities:

- present a compact desktop interface,
- handle the user-provided login workflow,
- automate the browser session,
- extract selected data from the web workflow,
- map extracted data into predefined export columns,
- generate an Excel workbook.

Key technologies:

- Python
- Selenium
- customtkinter
- openpyxl
- PyInstaller

## Android Application

The Android application is implemented as a native Java app using WebView.

Main responsibilities:

- open the web workflow inside the app,
- let the user authenticate in WebView,
- run the extraction workflow in the mobile context,
- map extracted values to the same export structure,
- save the Excel output through Android document handling.

Key technologies:

- Java
- Android SDK
- WebView
- XLSX export workflow

## Extraction Process

The extraction process is designed around selected records and detail pages. It reads web page content, collects the relevant values and applies explicit mapping rules.

The public documentation intentionally avoids sensitive operational details, private credentials, cookies, tokens or real business data.

## Excel Export

The export workflow creates a structured workbook with fixed column names. The goal is not to fill every possible column at any cost, but to keep key logistics fields accurate and easy to review.

Important exported data groups include:

- reference and search identifiers,
- pickup data,
- delivery data,
- planned dates,
- vehicle or goods parameters.

## Portfolio Organization

The repository is organized to present:

- the desktop implementation,
- the Android implementation,
- shared business mapping assumptions,
- documentation for architecture, workflow, security and future improvements.

Generated binaries, local build outputs and private signing artifacts are excluded from version control.

