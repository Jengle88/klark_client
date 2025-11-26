package ru.jengle88.klerk_client.ui.generatedocs

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class GenerateDocsStateModel : ScreenModel {

    private val _state = MutableStateFlow(GenerateDocsParamsState.EMPTY)
    val state = _state.asStateFlow()

    private val _effect = MutableSharedFlow<GenerateDocsEffect>()
    val effect = _effect.asSharedFlow()

    init {
        onIntent(
            GenerateDocsIntent.ReceiveTableData(
                listOf(
                    listOf("Заголовок 1", "Заголовок 2", "Заголовок 3", "Заголовок 4"),
                    listOf("Данные 1.1", "Данные 1.2", "Данные 1.3", "Данные 1.4"),
                    listOf("Данные 2.1", "Данные 2.2", "Данные 2.3", "Данные 2.4"),
                    listOf("Данные 3.1", "Данные 3.2", "Данные 3.3", "Данные 3.4"),
                    listOf("Данные 4.1", "Данные 4.2", "Данные 4.3", "Данные 4.4"),
                )
            )
        )
    }
    fun onIntent(intent: GenerateDocsIntent) {
        when (intent) {
            is GenerateDocsIntent.StartGenerating -> generate()
            is GenerateDocsIntent.ReceiveTableData -> onReceiveTableData(intent.data)
            else -> TODO()
        }
    }

    fun onReceiveTableData(data: List<List<String>>) {
        _state.update {
            it.copy(tableData = data.map { it.toPersistentList() }.toPersistentList())
        }
    }

    private fun generate() {
        screenModelScope.launch {
            _state.update { it.copy(isGenerating = true) }
            // Simulate generation
            delay(3000)
            _state.update { it.copy(isGenerating = false) }
        }
    }
}
