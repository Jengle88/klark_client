package ru.jengle88.klerk_client.ui.generatedocs

sealed class GenerateDocsEffect {
    data object ShowTablePicker : GenerateDocsEffect()
    data object ShowTemplatePicker : GenerateDocsEffect()
    data object ShowDestinationPicker : GenerateDocsEffect()
    data class ShowProcessingBottomSheet(
        val tableData: List<List<String>>,
        val pathToTemplate: String,
        val pathToDestination: String,
        val ignoreLastNColumn: Int,
        val unionLastNColumn: Int,
    ) : GenerateDocsEffect()
}