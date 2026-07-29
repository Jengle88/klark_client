package ru.jengle88.klarkclient.ui.generatedocs

sealed class GenerateDocsIntent {
    data class UpdatePathToTable(val path: String) : GenerateDocsIntent()

    data class UpdatePathToTemplate(val path: String) : GenerateDocsIntent()

    data class UpdatePathToDestination(val path: String) : GenerateDocsIntent()

    data class UpdateIgnoreLastNColumn(val value: Int?) : GenerateDocsIntent()

    data class UpdateUnionLastNColumn(val value: Int?) : GenerateDocsIntent()

    data class UpdateIsTableGrouped(val value: Boolean) : GenerateDocsIntent()

    data class ReceiveTableData(val data: List<List<String>>) : GenerateDocsIntent()

    data object StartGenerating : GenerateDocsIntent()

    data object ShowInfo : GenerateDocsIntent()
}
