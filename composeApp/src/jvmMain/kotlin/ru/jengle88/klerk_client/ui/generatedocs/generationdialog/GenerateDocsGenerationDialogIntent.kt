package ru.jengle88.klerk_client.ui.generatedocs.generationdialog

sealed class GenerateDocsGenerationDialogIntent {
    data class StartGeneration(
        val tableData: List<List<String>>,
        val pathToTemplate: String,
        val pathToDestination: String,
        val ignoreLastNColumn: Int,
        val unionLastNColumn: Int
    ) : GenerateDocsGenerationDialogIntent()
    data object StopGeneration : GenerateDocsGenerationDialogIntent()
}
