package ru.jengle88.klarkclient.ui.generatedocs

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import ru.jengle88.klarkclient.data.document.TableGroup

data class GenerateDocsParamsState(
    val pathToTable: String,
    val pathToTemplate: String,
    val pathToDestination: String,
    val ignoreLastNColumn: Int?,
    val unionLastNColumn: Int?,
    val isTableGrouped: Boolean,
    val isGenerating: Boolean,
    val isTableLoading: Boolean,
    val tableData: ImmutableList<ImmutableList<String>>,
    val tableGroups: ImmutableList<TableGroup>,
) {
    companion object {
        val EMPTY =
            GenerateDocsParamsState(
                pathToTable = "",
                pathToTemplate = "",
                pathToDestination = "",
                ignoreLastNColumn = null,
                unionLastNColumn = null,
                isTableGrouped = true,
                isGenerating = false,
                isTableLoading = false,
                tableData = persistentListOf(),
                tableGroups = persistentListOf(),
            )
    }
}
