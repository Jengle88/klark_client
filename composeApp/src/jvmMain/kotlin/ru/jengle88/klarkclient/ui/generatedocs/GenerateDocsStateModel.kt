package ru.jengle88.klarkclient.ui.generatedocs

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.jengle88.klarkclient.domain.usecase.GroupTableRowsByFirstColumnUseCase
import ru.jengle88.klarkclient.domain.usecase.ReadTableDataUseCase

class GenerateDocsStateModel(
    private val readTableDataUseCase: ReadTableDataUseCase,
    private val groupTableRowsByFirstColumnUseCase: GroupTableRowsByFirstColumnUseCase,
) : ScreenModel {
    private val _state = MutableStateFlow(GenerateDocsParamsState.EMPTY)
    val state = _state.asStateFlow()

    private val _effect = MutableSharedFlow<GenerateDocsEffect>()
    val effect = _effect.asSharedFlow()

    val supportedTableFormat = listOf("xlsx", "xls")

    fun onIntent(intent: GenerateDocsIntent) {
        when (intent) {
            is GenerateDocsIntent.StartGenerating -> generate()
            is GenerateDocsIntent.ShowInfo -> showInfoDialog()
            is GenerateDocsIntent.ReceiveTableData -> {
                val tableData = intent.data.map { row -> row.toPersistentList() }.toPersistentList()
                val tableGroups = groupTableRowsByFirstColumnUseCase(intent.data).toImmutableList()
                _state.update { prevState ->
                    prevState.copy(
                        tableData = tableData,
                        tableGroups = tableGroups,
                    )
                }
            }

            is GenerateDocsIntent.UpdatePathToTable -> {
                _state.update { it.copy(pathToTable = intent.path) }
                updateTableData()
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
                updateTableData()
            }

            is GenerateDocsIntent.UpdateUnionLastNColumn -> {
                val coerceValue = intent.value?.coerceAtMost(100)
                _state.update { it.copy(unionLastNColumn = coerceValue) }
                updateTableData()
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

    private fun updateTableData() {
        val currentState = _state.value
        _state.update { it.copy(isTableLoading = true) }
        screenModelScope.launch(Dispatchers.IO) {
            val rawData =
                readTableDataUseCase(
                    currentState.pathToTable,
                    currentState.ignoreLastNColumn,
                    currentState.unionLastNColumn,
                )
            val tableData = rawData.map { it.toPersistentList() }.toPersistentList()
            val tableGroups = groupTableRowsByFirstColumnUseCase(rawData).toImmutableList()
            _state.update {
                it.copy(
                    isTableLoading = false,
                    tableData = tableData,
                    tableGroups = tableGroups,
                )
            }
        }
    }
}
