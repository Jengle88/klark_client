# Code Review for PR #36: Refactoring for CLEAN and move M2 to M3

## Summary
The Pull Request introduces significant refactoring to the Authentication and Document Generation domains, attempting to align with Clean Architecture principles and migrating UI components to Material 3.

However, a **CRITICAL** architectural issue is the complete absence of `commonMain` source set integration. All "domain" and "data" logic resides in `jvmMain`, violating the core KMP separation principle. Additionally, critical resource leaks and data correctness issues were identified in the document generation logic.

## 🔴 Critical Issues

### 1. Architecture & KMP Separation
- **File:** `composeApp/src/commonMain` (Missing)
- **Issue:** The project lacks a `commonMain` source set. All business logic (UseCases), Domain Models, and UI are located in `jvmMain`. This makes the application a standard JVM Desktop app, not a Kotlin Multiplatform app, despite the build configuration.
- **Recommendation:** Move all platform-agnostic code (Domain models, UseCases, Interfaces, ViewModels/ScreenModels) to `commonMain`. Use `expect/actual` for platform-specific implementations (e.g., File I/O, Preferences).

### 2. Resource Leaks in Document Generation
- **File:** `composeApp/src/jvmMain/kotlin/ru/jengle88/klarkclient/data/document/WordDocumentEditorDocxImpl.kt`
- **File:** `composeApp/src/jvmMain/kotlin/ru/jengle88/klarkclient/domain/usecase/GenerateWordFromTableUseCase.kt`
- **Issue:** `WordDocumentEditorFactory` creates a `WordDocumentEditor` which holds an open `InputStream` (via `XWPFDocument`), but `GenerateWordFromTableUseCase` never closes it. The `WordDocumentEditor` interface does not extend `AutoCloseable` or provide a `close()` method.
- **Recommendation:**
  1. Make `WordDocumentEditor` extend `AutoCloseable`.
  2. Implement `close()` in `WordDocumentEditorDocxImpl` to close the `XWPFDocument` (and its package/stream).
  3. Use `.use { ... }` block in `GenerateWordFromTableUseCase` when creating the editor.

### 3. Data Correctness in Table Parsing
- **File:** `composeApp/src/jvmMain/kotlin/ru/jengle88/klarkclient/domain/usecase/GenerateWordFromTableUseCase.kt`
- **Line:** ~97: `val rowWithRemovedEmptyCell = row.filter { it.isNotEmpty() }.drop(1)`
- **Issue:** Filtering empty cells (`filter { it.isNotEmpty() }`) before mapping to masks destroys column alignment. If a row has an empty cell in the middle (e.g., `[Name, , City]`), the subsequent values will shift left, mapping "City" to the "Age" mask.
- **Recommendation:** Remove `filter { it.isNotEmpty() }`. Handle empty strings explicitly in the loop or ensure the `masks` list aligns with the raw row indices.

## 🟡 Moderate Issues

### 4. Concurrency & Race Conditions
- **File:** `composeApp/src/jvmMain/kotlin/ru/jengle88/klarkclient/ui/generatedocs/GenerateDocsStateModel.kt`
- **Line:** `updateTableData` function
- **Issue:** `updateTableData` launches a new coroutine on `Dispatchers.IO` every time input changes (e.g., path updates), without cancelling previous jobs. This leads to race conditions where stale data might overwrite newer data.
- **Recommendation:** Keep a reference to the current `Job` and cancel it before starting a new one, or use `collectLatest` / `transformLatest` pattern if observing a flow of parameters.

### 5. Algorithmic Efficiency
- **File:** `composeApp/src/jvmMain/kotlin/ru/jengle88/klarkclient/domain/usecase/GenerateWordFromTableUseCase.kt`
- **Line:** `getMasks(masksFile)` inside loop
- **Issue:** The mask file is re-read and parsed from disk for *every single row* in the table. For a table with 1000 rows, this results in 1000 file reads.
- **Recommendation:** Cache the masks in a `Map<File, List<String>>` keyed by the template folder or mask file path to read it only once per unique template.

### 6. Blocking I/O in Auth Logic
- **File:** `composeApp/src/jvmMain/kotlin/ru/jengle88/klarkclient/domain/usecase/auth/LoginUseCase.kt`
- **Issue:** `authStore.saveAuth` uses `java.util.prefs.Preferences` which performs blocking I/O. While `LoginUseCase` is called on `Dispatchers.IO` in the current codebase, the UseCase itself does not enforce this dispatcher for the `saveAuth` call, relying on the caller.
- **Recommendation:** Wrap the entire `invoke` body or the blocking parts in `withContext(dispatcher.io)`.

## 💡 Minor Issues & Suggestions

### 7. UI/UX: Click Feedback
- **File:** `composeApp/src/jvmMain/kotlin/ru/jengle88/klarkclient/ui/auth/AuthStatusRectangleAvatar.kt`
- **Issue:** `clickable` is used with `indication = null`, providing no visual feedback (ripple) when the user clicks the avatar.
- **Recommendation:** Remove `indication = null` or provide a custom indication.

### 8. Performance: Recomposition
- **File:** `composeApp/src/jvmMain/kotlin/ru/jengle88/klarkclient/ui/screen/main/MainTab.kt`
- **Issue:** `MainTab.options` is a Composable getter that creates a new `TabOptions` instance (and calls `remember`) every time it is accessed. Since `MainTabNavigationRailItem` accesses it twice (for icon and title), it triggers multiple compositions.
- **Recommendation:** Store `options` in a `val` inside `MainTabNavigationRailItem` to access it once per composition.

### 9. Security: Path Traversal
- **File:** `composeApp/src/jvmMain/kotlin/ru/jengle88/klarkclient/domain/usecase/GenerateWordFromTableUseCase.kt`
- **Issue:** `parseFileName` sanitizes filenames, but `getFolder` uses `row.first()` to construct a `File` path. If the Excel cell contains `../`, it could traverse directories.
- **Recommendation:** Use `File.canonicalPath` to ensure the resolved path is strictly within the intended template directory.

### 10. Testability
- **File:** `composeApp/src/jvmMain/kotlin/ru/jengle88/klarkclient/common/CoroutineDispatchers.kt`
- **Issue:** Dispatchers are hardcoded (e.g., `val io get() = Dispatchers.IO`). This makes it hard to inject `TestDispatcher` during unit testing.
- **Recommendation:** Use a data class with constructor parameters for dispatchers, defaulting to standard ones, so they can be overridden in tests.

## Decision
**⚠️ REQUEST CHANGES**

The code requires significant architectural adjustments (moving to `commonMain`) and fixes for critical resource leaks and data correctness issues before it can be merged.
