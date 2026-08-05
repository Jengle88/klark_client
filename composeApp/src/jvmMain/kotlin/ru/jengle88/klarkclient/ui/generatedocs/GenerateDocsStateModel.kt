package ru.jengle88.klarkclient.ui.generatedocs

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.jengle88.klarkclient.common.CoroutineDispatchers
import ru.jengle88.klarkclient.data.document.TableGroup
import ru.jengle88.klarkclient.domain.mapping.TemplateDataMapping
import ru.jengle88.klarkclient.domain.usecase.GroupTableRowsByFirstColumnUseCase
import ru.jengle88.klarkclient.domain.usecase.ReadTableDataUseCase
import ru.jengle88.klarkclient.domain.usecase.ReadTemplateMasksUseCase
import ru.jengle88.klarkclient.ui.datamodels.TableContentState
import ru.jengle88.klarkclient.ui.datamodels.TableGroupState

class GenerateDocsStateModel(
    private val readTableDataUseCase: ReadTableDataUseCase,
    private val readTemplateMasksUseCase: ReadTemplateMasksUseCase,
    private val groupTableRowsByFirstColumnUseCase: GroupTableRowsByFirstColumnUseCase,
    private val templateDataMapping: TemplateDataMapping,
    private val coroutineDispatchers: CoroutineDispatchers,
) : ScreenModel {
    private val _state = MutableStateFlow(GenerateDocsParamsState.EMPTY)
    val state = _state.asStateFlow()

    private val _effect = MutableSharedFlow<GenerateDocsEffect>()
    val effect = _effect.asSharedFlow()

    private var updateTableDataJob: Job? = null
    private var loadMasksJob: Job? = null

    val supportedTableFormat = listOf("xlsx", "xls")

    fun onIntent(intent: GenerateDocsIntent) {
        when (intent) {
            is GenerateDocsIntent.StartGenerating -> generate()
            is GenerateDocsIntent.ShowInfo -> showInfoDialog()
            is GenerateDocsIntent.ReceiveTableData -> {
                val tableData = intent.data.toTableContentState()
                val tableGroups = buildTableGroups(tableData.rows, _state.value.isTableGrouped)
                _state.update { prevState ->
                    prevState.copy(
                        tableData = tableData,
                        tableGroups = tableGroups,
                    )
                }
                loadMasks()
            }

            is GenerateDocsIntent.UpdatePathToTable -> {
                _state.update { it.copy(pathToTable = intent.path) }
                if (intent.path.isNotBlank()) {
                    updateTableData()
                }
            }

            is GenerateDocsIntent.UpdatePathToTemplate -> {
                _state.update { it.copy(pathToTemplate = intent.path) }
                loadMasks()
            }

            is GenerateDocsIntent.UpdatePathToDestination -> {
                _state.update { it.copy(pathToDestination = intent.path) }
            }

            is GenerateDocsIntent.UpdateIgnoreLastNColumn -> {
                val coerceValue = intent.value?.coerceAtMost(100)
                _state.update { it.copy(ignoreLastNColumn = coerceValue) }
                if (_state.value.tableData.rows.isNotEmpty()) {
                    updateTableData()
                }
            }

            is GenerateDocsIntent.UpdateUnionLastNColumn -> {
                val coerceValue = intent.value?.coerceAtMost(100)
                _state.update { it.copy(unionLastNColumn = coerceValue) }
                if (_state.value.tableData.rows.isNotEmpty()) {
                    updateTableData()
                }
            }

            is GenerateDocsIntent.UpdateIsTableGrouped -> {
                _state.update { prevState ->
                    prevState.copy(
                        isTableGrouped = intent.value,
                        tableGroups =
                        if (prevState.tableData.rows.isNotEmpty()) {
                            buildTableGroups(prevState.tableData.rows, intent.value)
                        } else {
                            prevState.tableGroups
                        },
                    )
                }
                loadMasks()
            }
        }
    }

    fun onEffect(effect: GenerateDocsEffect) {
        screenModelScope.launch {
            _effect.emit(effect)
        }
    }

    private fun generate() {
        screenModelScope.launch {
            val snapshotOfState = _state.value

            _effect.emit(
                GenerateDocsEffect.ShowProcessingBottomSheet(
                    snapshotOfState.tableData.rows,
                    snapshotOfState.pathToTemplate,
                    snapshotOfState.pathToDestination,
                    snapshotOfState.ignoreLastNColumn ?: 0,
                    snapshotOfState.unionLastNColumn ?: 0,
                ),
            )
        }
    }

    private fun showInfoDialog() {
        screenModelScope.launch {
            _effect.emit(GenerateDocsEffect.ShowInfoBottomSheet)
        }
    }

    private fun loadMasks() {
        loadMasksJob?.cancel()
        val currentState = _state.value
        val pathToTemplate = currentState.pathToTemplate
        if (pathToTemplate.isBlank() ||
            currentState.tableGroups.isEmpty() ||
            !currentState.isTableGrouped
        ) {
            _state.update { it.copy(masksByGroupKey = persistentMapOf()) }
            return
        }

        loadMasksJob =
            screenModelScope.launch(coroutineDispatchers.io) {
                val masksByGroupKey =
                    currentState.tableGroups
                        .associate { group ->
                            group.key to
                                readTemplateMasksUseCase(
                                    pathToTemplate,
                                    group.key,
                                ).toImmutableList()
                        }
                        .toImmutableMap()
                ensureActive()
                _state.update { it.copy(masksByGroupKey = masksByGroupKey) }
            }
    }

    private fun updateTableData() {
        updateTableDataJob?.cancel()
        val currentState = _state.value
        _state.update { it.copy(isTableLoading = true) }
        updateTableDataJob =
            screenModelScope.launch(coroutineDispatchers.io) {
                val rawData =
                    readTableDataUseCase(
                        currentState.pathToTable,
                        currentState.ignoreLastNColumn,
                        currentState.unionLastNColumn,
                    )
                val tableData = rawData.toTableContentState()
                val tableGroups = buildTableGroups(tableData.rows, currentState.isTableGrouped)
                ensureActive()
                _state.update {
                    it.copy(
                        isTableLoading = false,
                        tableData = tableData,
                        tableGroups = tableGroups,
                    )
                }
                loadMasks()
            }
    }

    private fun buildTableGroups(
        rows: List<List<String>>,
        isGrouped: Boolean,
    ): ImmutableList<TableGroupState> = if (isGrouped) {
        groupTableRowsByFirstColumnUseCase(rows)
            .map { it.toTableGroupState() }
            .toImmutableList()
    } else {
        persistentListOf()
    }

    private fun TableGroup.toTableGroupState(): TableGroupState = TableGroupState(
        key = key,
        content =
        TableContentState(
            content.rows
                .map { row ->
                    (
                        listOf(
                            row.first(),
                        ) + templateDataMapping.getRowValues(row)
                        ).toPersistentList()
                }.toPersistentList(),
        ),
    )

    private fun List<List<String>>.toTableContentState(): TableContentState = TableContentState(
        map { row -> row.toPersistentList() }.toPersistentList(),
    )

    override fun onDispose() {
        updateTableDataJob?.cancel()
        loadMasksJob?.cancel()
    }
}
