package ru.jengle88.klarkclient.ui.generatedocs

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import ru.jengle88.klarkclient.ui.datamodels.TableContentState
import ru.jengle88.klarkclient.ui.datamodels.TableGroupState

data class GenerateDocsParamsState(
    val pathToTable: String,
    val pathToTemplate: String,
    val pathToDestination: String,
    val ignoreLastNColumn: Int?,
    val unionLastNColumn: Int?,
    val isTableGrouped: Boolean,
    val isGenerating: Boolean,
    val isTableLoading: Boolean,
    val tableData: TableContentState,
    val tableGroups: ImmutableList<TableGroupState>,
    val masksByGroupKey: ImmutableMap<String, ImmutableList<String>>,
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
                tableData = TableContentState(persistentListOf()),
                tableGroups = persistentListOf(),
                masksByGroupKey = persistentMapOf(),
            )
    }
}
