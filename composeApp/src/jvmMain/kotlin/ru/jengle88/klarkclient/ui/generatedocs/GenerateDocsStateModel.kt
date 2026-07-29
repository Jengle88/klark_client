package ru.jengle88.klarkclient.ui.generatedocs

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
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
import ru.jengle88.klarkclient.domain.usecase.GroupTableRowsByFirstColumnUseCase
import ru.jengle88.klarkclient.domain.usecase.ReadTableDataUseCase

class GenerateDocsStateModel(
    private val readTableDataUseCase: ReadTableDataUseCase,
    private val groupTableRowsByFirstColumnUseCase: GroupTableRowsByFirstColumnUseCase,
    private val coroutineDispatchers: CoroutineDispatchers
) : ScreenModel {
    private val _state = MutableStateFlow(GenerateDocsParamsState.EMPTY)
    val state = _state.asStateFlow()

    private val _effect = MutableSharedFlow<GenerateDocsEffect>()
    val effect = _effect.asSharedFlow()

    private var updateTableDataJob: Job? = null

    val supportedTableFormat = listOf("xlsx", "xls")

    fun onIntent(intent: GenerateDocsIntent) {
        when (intent) {
            is GenerateDocsIntent.StartGenerating -> generate()
            is GenerateDocsIntent.ShowInfo -> showInfoDialog()
            is GenerateDocsIntent.ReceiveTableData -> {
                val tableData = intent.data.map { row -> row.toPersistentList() }.toPersistentList()
                val tableGroups = buildTableGroups(tableData, _state.value.isTableGrouped)
                _state.update { prevState ->
                    prevState.copy(
                        tableData = tableData,
                        tableGroups = tableGroups
                    )
                }
            }

            is GenerateDocsIntent.UpdatePathToTable -> {
                _state.update { it.copy(pathToTable = intent.path) }
                if (intent.path.isNotBlank()) {
                    updateTableData()
                }
            }

            is GenerateDocsIntent.UpdatePathToTemplate -> {
                _state.update { it.copy(pathToTemplate = intent.path) }
            }

            is GenerateDocsIntent.UpdatePathToDestination -> {
                _state.update { it.copy(pathToDestination = intent.path) }
            }

            is GenerateDocsIntent.UpdateIgnoreLastNColumn -> {
                val coerceValue = intent.value?.coerceAtMost(100)
                _state.update { it.copy(ignoreLastNColumn = coerceValue) }
                if (_state.value.tableData.isNotEmpty()) {
                    updateTableData()
                }
            }

            is GenerateDocsIntent.UpdateUnionLastNColumn -> {
                val coerceValue = intent.value?.coerceAtMost(100)
                _state.update { it.copy(unionLastNColumn = coerceValue) }
                if (_state.value.tableData.isNotEmpty()) {
                    updateTableData()
                }
            }

            is GenerateDocsIntent.UpdateIsTableGrouped -> {
                _state.update { prevState ->
                    prevState.copy(
                        isTableGrouped = intent.value,
                        tableGroups =
                        if (prevState.tableData.isNotEmpty()) {
                            buildTableGroups(prevState.tableData, intent.value)
                        } else {
                            prevState.tableGroups
                        }
                    )
                }
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
                    snapshotOfState.tableData,
                    snapshotOfState.pathToTemplate,
                    snapshotOfState.pathToDestination,
                    snapshotOfState.ignoreLastNColumn ?: 0,
                    snapshotOfState.unionLastNColumn ?: 0
                )
            )
        }
    }

    private fun showInfoDialog() {
        screenModelScope.launch {
            _effect.emit(GenerateDocsEffect.ShowInfoBottomSheet)
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
                        currentState.unionLastNColumn
                    )
                val tableData = rawData.map { it.toPersistentList() }.toPersistentList()
                val tableGroups = buildTableGroups(tableData, currentState.isTableGrouped)
                ensureActive()
                _state.update {
                    it.copy(
                        isTableLoading = false,
                        tableData = tableData,
                        tableGroups = tableGroups
                    )
                }
            }
    }

    private fun buildTableGroups(
        rows: List<List<String>>,
        isGrouped: Boolean
    ): ImmutableList<TableGroup> = if (isGrouped) {
        groupTableRowsByFirstColumnUseCase(rows).toImmutableList()
    } else {
        persistentListOf()
    }

    override fun onDispose() {
        updateTableDataJob?.cancel()
    }
}
