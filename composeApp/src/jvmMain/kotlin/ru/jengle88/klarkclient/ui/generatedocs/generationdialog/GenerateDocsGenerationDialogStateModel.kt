package ru.jengle88.klarkclient.ui.generatedocs.generationdialog

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.jengle88.klarkclient.domain.usecase.GenerateWordFromTableUseCase
import ru.jengle88.klarkclient.ui.generatedocs.GenerateDocsGenerationDialogState

class GenerateDocsGenerationDialogStateModel(
    private val generateWordFromTableUseCase: GenerateWordFromTableUseCase
) : ScreenModel {
    private val _state = MutableStateFlow(GenerateDocsGenerationDialogState.EMPTY)
    val state = _state.asStateFlow()

    private val _effect = MutableSharedFlow<GenerateDocsGenerationDialogEffect>()
    val effect = _effect.asSharedFlow()

    private var generationJob: Job? = null

    fun onIntent(intent: GenerateDocsGenerationDialogIntent) {
        when (intent) {
            is GenerateDocsGenerationDialogIntent.StartGeneration -> {
                generate(intent)
            }
            GenerateDocsGenerationDialogIntent.StopGeneration -> {
                stopGenerating()
            }
        }
    }

    private fun stopGenerating() {
        generationJob?.cancel()
        generationJob = null
        screenModelScope.launch {
            _effect.emit(GenerateDocsGenerationDialogEffect.DismissDialog)
        }
    }

    private fun generate(intent: GenerateDocsGenerationDialogIntent.StartGeneration) {
        generationJob =
            generateWordFromTableUseCase
                .invoke(
                    intent.tableData,
                    intent.pathToTemplate,
                    intent.pathToDestination,
                    intent.ignoreLastNColumn,
                    intent.unionLastNColumn
                ).flowOn(Dispatchers.IO)
                .onEach { newState ->
                    when (newState) {
                        GenerateWordFromTableUseCase.WorkStatus.Start -> {
                            _state.update {
                                it.copy(isGenerating = true, steps = persistentListOf())
                            }
                        }

                        is GenerateWordFromTableUseCase.WorkStatus.Step -> {
                            _state.update { prevState ->
                                prevState.copy(
                                    steps = (
                                        prevState.steps + listOf(
                                            newState.message
                                        )
                                        ).toImmutableList()
                                )
                            }
                        }

                        is GenerateWordFromTableUseCase.WorkStatus.Finish -> {
                            _state.update { prevState ->
                                val steps =
                                    if (newState.message != null) {
                                        (
                                            prevState.steps + listOf(
                                                newState.message
                                            )
                                            ).toImmutableList()
                                    } else {
                                        prevState.steps
                                    }
                                prevState.copy(
                                    isGenerating = false,
                                    steps = steps,
                                    error = newState.cause?.message
                                )
                            }
                        }
                    }
                }.launchIn(screenModelScope)
    }

    override fun onDispose() {
        generationJob?.cancel()
        generationJob = null
    }
}
