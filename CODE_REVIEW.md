# Code Review for PR #36: Critical Architecture and Resource Issues

## Summary
The PR introduces significant changes, including new document processing logic (`ExcelDocumentDataProvider`, `WordDocumentEditor`), authentication state management (`AuthViewScreenModel`), and UI updates (`App.kt`). However, several **CRITICAL** issues were identified that prevent this PR from being mergeable. These include a fundamental lack of Kotlin Multiplatform (KMP) separation, resource leaks in file handling, security vulnerabilities (path traversal), broken tests, and incorrect UI theming.

**Approval Decision:** 🔴 **REQUEST CHANGES**

## 🔴 Critical Issues

### 1. Architecture: Missing KMP Separation
- **File:** `composeApp/src/jvmMain/kotlin/ru/jengle88/klarkclient` (Entire directory)
- **Issue:** The project places all domain logic (UseCases), data implementations, and UI code strictly within `jvmMain`. There is no `commonMain` source set.
- **Why:** This tightly couples the business logic to the JVM platform, defeating the purpose of a Multiplatform project.
- **Fix:** Move all platform-agnostic code (Domain Models, UseCases, ViewModels/ScreenModels, UI Components) to `commonMain`. Use `expect/actual` only for platform-specific implementations (e.g., File I/O, Preferences).

### 2. UI: Missing MaterialTheme Wrapper
- **File:** `composeApp/src/jvmMain/kotlin/ru/jengle88/klarkclient/main.kt`
- **File:** `composeApp/src/jvmMain/kotlin/ru/jengle88/klarkclient/ui/app/App.kt`
- **Line:** `main.kt`: `Window { App() }`
- **Issue:** The application entry point `App()` is not wrapped in a `MaterialTheme` provider.
- **Why:** Compose Material 3 components rely on `LocalColorScheme`, `LocalTypography`, etc., provided by `MaterialTheme`. Without it, the app may crash or render incorrectly on different platforms/themes.
- **Fix:** Wrap `App()` content in `MaterialTheme { ... }` in `main.kt` or at the root of `App.kt`.

### 3. Resources: Unclosed Streams
- **File:** `composeApp/src/jvmMain/kotlin/ru/jengle88/klarkclient/data/document/WordDocumentEditorDocxImpl.kt`
- **Line:** `document.write(dstFile.outputStream())` (saveToFile)
- **Line:** `val document = XWPFDocument(file.inputStream())` (createEditor)
- **Issue:** `FileOutputStream` and `FileInputStream` are opened but never closed. `XWPFDocument` also holds resources that should be managed.
- **Why:** Leaving streams open leads to file locks (especially on Windows) and resource exhaustion.
- **Fix:** Use `.use { ... }` block for streams. Implement `AutoCloseable` in `WordDocumentEditor` and ensure `XWPFDocument` is closed.

### 4. Security: Path Traversal Vulnerability
- **File:** `composeApp/src/jvmMain/kotlin/ru/jengle88/klarkclient/domain/usecase/GenerateWordFromTableUseCase.kt`
- **Function:** `processRow`, `parseFileName`
- **Issue:** The code constructs file paths using raw strings from Excel cells (`row.first()`) and template filenames. `parseFileName` replaces `/` and `\` but does not block `..` (parent directory traversal).
- **Why:** An attacker could craft an Excel file with a filename like `..\..\evil.docx` to overwrite files outside the intended directory.
- **Fix:** Validate that the canonical path of the destination file starts with the canonical path of the intended parent directory.

### 5. Testing: Broken Unit Tests
- **File:** `composeApp/src/jvmTest/kotlin/ru/jengle88/klarkclient/data/network/auth/AuthManagerTest.kt`
- **Line:** `whenever(authCodeReceiver.awaitAuthCode(...))`
- **Issue:** The test mocks `onServerReady` as `() -> Unit` (no arguments), but the actual interface `AuthCodeReceiver` expects `(Int) -> Unit`. This causes a `ClassCastException` at runtime.
- **Why:** Tests are failing, blocking CI/CD.
- **Fix:** Update the test to match the new signature: `val onServerReady = invocation.getArgument<(Int) -> Unit>(2); onServerReady(DEFAULT_PORT)`.

## 🟡 Moderate Issues

### 6. Data Correctness: Filtering Empty Cells Shifts Columns
- **File:** `composeApp/src/jvmMain/kotlin/ru/jengle88/klarkclient/domain/usecase/GenerateWordFromTableUseCase.kt`
- **Line:** `val rowWithRemovedEmptyCell = row.filter { it.isNotEmpty() }.drop(1)`
- **Issue:** Removing empty cells shifts subsequent column values to the left, causing misalignment with the defined masks. If column B is empty, column C moves to position B.
- **Why:** This results in data being mapped to the wrong template placeholders.
- **Fix:** Remove `filter { it.isNotEmpty() }`. Handle empty strings explicitly during mask application.

### 7. Efficiency: Inefficient Excel Parsing
- **File:** `composeApp/src/jvmMain/kotlin/ru/jengle88/klarkclient/data/document/ExcelDocumentDataProviderXlsxImpl.kt`
- **Function:** `readData`
- **Issue:** The implementation creates multiple intermediate collections (`map`, `padLast`, `dropLast`, `takeLast`, `toMutableList`) for every row.
- **Why:** This leads to high memory allocation and GC pressure for large files.
- **Fix:** Use `Sequence` operations or process data in a single pass without intermediate lists.

### 8. Concurrency: Race Condition in State Updates
- **File:** `composeApp/src/jvmMain/kotlin/ru/jengle88/klarkclient/ui/generatedocs/GenerateDocsStateModel.kt`
- **Function:** `updateTableData`
- **Issue:** Launches a new coroutine on `Dispatchers.IO` for every input change without cancelling the previous one.
- **Why:** Rapid input changes can lead to race conditions where older data overwrites newer data.
- **Fix:** Maintain a reference to the `Job` and cancel it before starting a new one, or use `collectLatest` on a flow of inputs.

### 9. Algorithm: Incorrect Text Replacement Logic
- **File:** `composeApp/src/jvmMain/kotlin/ru/jengle88/klarkclient/data/document/WordDocumentEditorDocxImpl.kt`
- **Function:** `replaceTextInParagraphs`
- **Issue:** The logic relies on `oldText.first()` and `oldText.last()` to drop characters from runs, which is fragile if the character appears multiple times or if the run boundary splits the text unexpectedly.
- **Why:** This can result in corrupted documents.
- **Fix:** Implement a more robust text replacement algorithm that handles split runs correctly (e.g., concatenate runs text, replace, and distribute back).

## 💡 Minor Issues

### 10. Formatting: Hardcoded Strings
- **File:** `composeApp/src/jvmMain/kotlin/ru/jengle88/klarkclient/domain/usecase/GenerateWordFromTableUseCase.kt`
- **Issue:** Strings like `"generated"`, `"шаблон.docx"`, `"маски.txt"` are hardcoded.
- **Fix:** Move these to constants or configuration.
