package ru.jengle88.klerk_client.ui.generatedocs

data class GenerateDocsParamsState(
    val pathToTable: String,
    val pathToTemplate: String,
    val pathToDestination: String,
    val ignoreLastNColumn: Int,
    val unionLastNColumn: Int,
) {

    companion object {
        val EMPTY = GenerateDocsParamsState(
            pathToTable = "",
            pathToTemplate = "",
            pathToDestination = "",
            ignoreLastNColumn = 0,
            unionLastNColumn = 0
        )
    }
}
