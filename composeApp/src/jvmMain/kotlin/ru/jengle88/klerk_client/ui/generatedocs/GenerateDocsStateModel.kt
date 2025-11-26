package ru.jengle88.klerk_client.ui.generatedocs

import cafe.adriel.voyager.core.model.ScreenModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

class GenerateDocsStateModel : ScreenModel {

    private val _state = MutableStateFlow(GenerateDocsParamsState.EMPTY)
    val state = _state.asStateFlow()

    private val _effect = MutableSharedFlow<GenerateDocsEffect>()
    val effect = _effect.asSharedFlow()

    fun onIntent(intent: GenerateDocsIntent) {
    }
}