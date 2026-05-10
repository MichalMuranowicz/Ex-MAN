# Workflow

This document describes the Ex-MAN workflow in a public, portfolio-friendly way. It avoids real credentials, company data, exported operational records and session details.

## User Workflow

1. Start the application.
2. Authenticate in the web workflow.
3. Navigate to the relevant operational view.
4. Start the extraction process.
5. Extract selected data from matching records.
6. Map data into structured fields.
7. Export the result to Excel.

## Desktop Workflow

The Windows desktop version provides a local application window where the user enters credentials, selects an export location and starts the automation process.

The application opens a browser session, works through the web workflow and exports the selected logistics data to an `.xlsx` workbook.

## Android Workflow

The Android version opens the web workflow in a WebView. The user authenticates in the WebView, navigates to the relevant data view and starts the extraction process with a single main action.

After the export is completed, the Android workflow saves the workbook through the system document flow.

## Data Mapping Workflow

The extraction process maps source values into a stable Excel layout. The most important groups are:

- reference and search identifiers,
- pickup date and address,
- delivery date and address,
- vehicle or goods details,
- contact and operational fields where available.

The mapping is explicit so that the export is predictable and can be reviewed by the user.

## Excel Export Workflow

The final output is an Excel workbook named `MAN extract.xlsx`.

Generated Excel exports are treated as operational output and should not be committed to the repository.

