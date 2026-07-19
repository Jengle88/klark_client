# Klark Client

A desktop application for document automation and legal tasks. It generates Word documents from Excel tables based on templates.

Built with Kotlin and Compose Multiplatform, packaged for Desktop (JVM).

## What it does

- Generate dozens or hundreds of similar Word documents from a single Excel table.
- Use flexible DOCX templates with placeholder masks.
- Keep source data, templates, and generated results organized in separate folders.

## Features

- **Word document generation** — substitute values from Excel rows into DOCX templates.
- **Excel column control** — ignore or merge the last N columns when reading the table.
- **Update checker** — compare the current app version with the latest GitHub Release.
- **Easy-to-use UI** — desktop app with screens for file selection, settings, and generation progress.

## System requirements

- Windows, macOS, or Linux
- Java Runtime Environment (JRE) 17 or newer

## How to use

### Prepare templates

1. Create a templates folder.
2. Add one subfolder for each document type.
3. Each subfolder must contain two files:
   - `шаблон.docx` — a Word document with replacement masks like `{{MASK}}`.
   - `маски.txt` — a list of masks the app will look for in the template.

### Prepare the Excel file

1. The first row should contain column headers.
2. Each remaining row represents one future document.
3. Column headers are used to map Excel values to template masks.

### Generate documents

1. Launch the application.
2. On the generation screen, select:
   - The Excel file with data.
   - The templates folder.
   - The destination folder for generated documents.
3. Optionally configure column settings (how many columns to ignore or merge).
4. Click the generate button and wait for completion.
5. Generated documents will appear under `generated/<template type>/<filename>.docx`.

## Tech stack

- Kotlin Multiplatform + Compose Desktop
- Voyager — navigation and screen state management
- Koin — dependency injection
- Apache POI — Excel reading and Word generation

## License

TBD.
