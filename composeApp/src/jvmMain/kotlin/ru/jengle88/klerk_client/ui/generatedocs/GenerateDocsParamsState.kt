package ru.jengle88.klerk_client.ui.generatedocs

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class GenerateDocsParamsState(
    val pathToTable: String,
    val pathToTemplate: String,
    val pathToDestination: String,
    val ignoreLastNColumn: Int,
    val unionLastNColumn: Int,
    val isGenerating: Boolean,
    val tableData: ImmutableList<ImmutableList<String>>
) {

    companion object {
        val EMPTY = GenerateDocsParamsState(
            pathToTable = "",
            pathToTemplate = "",
            pathToDestination = "",
            ignoreLastNColumn = 0,
            unionLastNColumn = 0,
            isGenerating = false,
            tableData = persistentListOf()
        )
    }
}
