package ru.jengle88.klerk_client.ui.generatedocs

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class GenerateDocsParamsState(
    val pathToTable: String,
    val pathToTemplate: String,
    val pathToDestination: String,
    val ignoreLastNColumn: Int?,
    val unionLastNColumn: Int?,
    val isGenerating: Boolean,
    val isTableLoading: Boolean,
    val tableData: ImmutableList<ImmutableList<String>>
) {

    companion object {
        val EMPTY = GenerateDocsParamsState(
            pathToTable = "",
            pathToTemplate = "",
            pathToDestination = "",
            ignoreLastNColumn = null,
            unionLastNColumn = null,
            isGenerating = false,
            isTableLoading = false,
            tableData = persistentListOf()
        )
    }
}
