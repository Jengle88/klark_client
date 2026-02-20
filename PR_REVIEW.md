# Code Review for PR #36: Refactoring and Modernization

## Summary
The Pull Request introduces significant refactoring to the Authentication and Document Generation domains, attempting to align with Clean Architecture principles and migrating UI components to Material 3.

However, several **CRITICAL** issues were identified that prevent this PR from being mergeable. The most significant is the complete lack of Kotlin Multiplatform (KMP) separation, rendering the project a standard JVM application despite its structure. Additionally, critical resource leaks, data correctness bugs, broken tests, and UI theming issues must be addressed.

## 🔴 Critical Issues

### 1. Architecture: Missing KMP Separation
- **File:** `composeApp/src/commonMain` (Missing)
- **Files:** All source files under `composeApp/src/jvmMain/kotlin/ru/jengle88/klarkclient`
- **Issue:** The project violates KMP principles by placing all domain logic (UseCases), data implementations, and UI code strictly within `jvmMain`. There is no `commonMain` source set.
- **Why:** This tightly couples the business logic to the JVM platform, defeating the purpose of a Multiplatform project.
- **Fix:** Move all platform-agnostic code (Domain Models, UseCases, ViewModels/ScreenModels, UI Components) to `commonMain`. Use `expect/actual` only for platform-specific implementations (e.g., File I/O, Preferences).

### 2. UI: Missing MaterialTheme Wrapper
- **File:** `composeApp/src/jvmMain/kotlin/ru/jengle88/klarkclient/main.kt`
- **File:** `composeApp/src/jvmMain/kotlin/ru/jengle88/klarkclient/ui/app/App.kt`
- **Issue:** The application entry point `App()` is not wrapped in a `MaterialTheme` provider. While individual components (like `NavigationRail`) might work, they will lack consistent styling (Typography, Shapes, ColorScheme) and may crash or render incorrectly on different platforms/themes.
- **Why:** Compose Material 3 components rely on `LocalColorScheme`, `LocalTypography`, etc., provided by `MaterialTheme`.
- **Fix:** Wrap `App()` content in `MaterialTheme { ... }` in `main.kt` or at the root of `App.kt`.

### 3. Coroutines & Resources: Unclosed Streams
- **File:** `composeApp/src/jvmMain/kotlin/ru/jengle88/klarkclient/data/document/WordDocumentEditorDocxImpl.kt`
- **Functions:** `saveToFile`, `createEditor`
- **Issue:**
  - `createEditor`: Opens `FileInputStream` but passes it to `XWPFDocument` without ensuring closure.
  - `saveToFile`: Creates `FileOutputStream` via `dstFile.outputStream()` but never closes it.
- **Why:** Leaving streams open leads to file locks (especially on Windows) and resource exhaustion.
- **Fix:** Use `.use { ... }` block for streams. Implement `AutoCloseable` in `WordDocumentEditor` and ensure `XWPFDocument` is closed.

### 4. Algorithm: Incorrect Text Replacement Logic
- **File:** `composeApp/src/jvmMain/kotlin/ru/jengle88/klarkclient/data/document/WordDocumentEditorDocxImpl.kt`
- **Function:** `replaceTextInParagraphs`
- **Issue:** The logic for handling multi-run replacements (where a search term spans across multiple text runs) is flawed. It attempts to reconstruct text by dropping characters based on `oldText.first()`/`last()`, which fails if the character appears multiple times or if the match is not at the assumed position. Apache POI's `TextSegment` does not provide enough context (character offsets) for this simplistic approach.
- **Why:** This will result in corrupted documents where text is incorrectly replaced or partially deleted.
- **Fix:** Re-implement using a robust algorithm (e.g., concatenating all runs text, performing replacement, and distributing back, or using a library that handles this).

### 5. Data Correctness: Filtering Empty Cells
- **File:** `composeApp/src/jvmMain/kotlin/ru/jengle88/klarkclient/domain/usecase/GenerateWordFromTableUseCase.kt`
- **Line:** `val rowWithRemovedEmptyCell = row.filter { it.isNotEmpty() }.drop(1)`
- **Issue:** Removing empty cells shifts subsequent column values to the left, causing misalignment with the defined masks.
- **Why:** If row is `[Name, , City]`, it becomes `[Name, City]`. "City" might map to "Age" mask.
- **Fix:** Remove `filter { it.isNotEmpty() }`. Handle empty strings explicitly during mask application.

### 6. Testing: Broken Tests
- **File:** `composeApp/src/jvmTest/kotlin/ru/jengle88/klarkclient/data/network/auth/AuthManagerTest.kt`
- **Issue:** Unit tests are failing with `ClassCastException` because the test mocks `AuthCodeReceiver.awaitAuthCode` assuming `onServerReady` is `() -> Unit`, but the interface was changed to `(Int) -> Unit`.
- **Why:** CI/CD pipelines will fail, and the feature is not verified.
- **Fix:** Update the test to match the new signature: `val onServerReady = invocation.getArgument<(Int) -> Unit>(2); onServerReady(DEFAULT_PORT)`.

## 🟡 Moderate Issues

### 7. Compose: Race Condition in State Updates
- **File:** `composeApp/src/jvmMain/kotlin/ru/jengle88/klarkclient/ui/generatedocs/GenerateDocsStateModel.kt`
- **Function:** `updateTableData`
- **Issue:** Launches a new coroutine on `Dispatchers.IO` for every input change without cancelling the previous one.
- **Why:** Rapid input changes can lead to race conditions where older data overwrites newer data.
- **Fix:** Maintain a reference to the `Job` and cancel it before starting a new one.

### 8. Algorithm: Inefficient Excel Parsing
- **File:** `composeApp/src/jvmMain/kotlin/ru/jengle88/klarkclient/data/document/ExcelDocumentDataProviderXlsxImpl.kt`
- **Function:** `readData`
- **Issue:** Uses multiple intermediate collections (`map`, `padLast`, `dropLast`, `takeLast`, `toMutableList`, `apply`, `dropLastWhile`) for every row.
- **Why:** High memory allocation and GC pressure for large files.
- **Fix:** Use `Sequence` operations or process data in a single pass.

### 9. Security: Path Traversal Potential
- **File:** `composeApp/src/jvmMain/kotlin/ru/jengle88/klarkclient/domain/usecase/GenerateWordFromTableUseCase.kt`
- **Function:** `getFolder` / `parseFileName`
- **Issue:** Constructs file paths using raw strings from Excel cells (`row.first()`). While `parseFileName` replaces separators, `..` (parent directory) is not explicitly blocked.
- **Why:** Potential for writing files outside the intended directory if malicious input is provided.
- **Fix:** Use `File(parent, child).canonicalPath.startsWith(parent.canonicalPath)` check or strictly validate filenames.

## Decision
**⚠️ REQUEST CHANGES**

Please address the critical architecture, correctness, and testing issues before merging.
