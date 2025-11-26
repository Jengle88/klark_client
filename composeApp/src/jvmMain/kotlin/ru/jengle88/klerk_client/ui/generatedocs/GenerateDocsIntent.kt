package ru.jengle88.klerk_client.ui.generatedocs

sealed class GenerateDocsIntent {
    data object ShowPathToTablePicker : GenerateDocsIntent()
    data object ShowPathToTemplatePicker : GenerateDocsIntent()
    data object ShowPathToDestinationPicker : GenerateDocsIntent()
    data class UpdatePathToTable(val path: String) : GenerateDocsIntent()
    data class UpdatePathToTemplate(val path: String) : GenerateDocsIntent()
    data class UpdatePathToDestination(val path: String) : GenerateDocsIntent()
    data class UpdateIgnoreLastNColumn(val value: Int) : GenerateDocsIntent()
    data class UpdateUnionLastNColumn(val value: Int) : GenerateDocsIntent()
}
