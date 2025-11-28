package ru.jengle88.klerk_client.ui.generatedocs

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ru.jengle88.klerk_client.data.XlsxDataProvider

class GenerateDocsStateModel(
    private val xlsxDataProvider: XlsxDataProvider,
) : ScreenModel {

    private val _state = MutableStateFlow(GenerateDocsParamsState.EMPTY)
    val state = _state.asStateFlow()

    private val _effect = MutableSharedFlow<GenerateDocsEffect>()
    val effect = _effect.asSharedFlow()

    fun onIntent(intent: GenerateDocsIntent) {
        when (intent) {
            is GenerateDocsIntent.StartGenerating -> generate()
            is GenerateDocsIntent.ReceiveTableData -> {
                _state.update { prevState ->
                    prevState.copy(tableData = intent.data.map { row -> row.toPersistentList() }.toPersistentList())
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

            _effect.emit(GenerateDocsEffect.ShowProcessingBottomSheet(
                snapshotOfState.tableData,
                snapshotOfState.pathToTemplate,
                snapshotOfState.pathToDestination,
                snapshotOfState.ignoreLastNColumn ?: 0,
                snapshotOfState.unionLastNColumn ?: 0
            ))
        }
    }

    private fun updateTableData() {
        val currentState = _state.value
        _state.update { it.copy(isTableLoading = true) }
        screenModelScope.launch(Dispatchers.IO) {
            val data = xlsxDataProvider.readData(
                currentState.pathToTable,
                currentState.ignoreLastNColumn ?: 0,
                currentState.unionLastNColumn ?: 0
            ).map { it.toPersistentList() }.toPersistentList()
            _state.update { it.copy(isTableLoading = false, tableData = data) }
        }
    }
}
