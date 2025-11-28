package ru.jengle88.klerk_client.ui.generatedocs.generationdialog

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.bottomSheet.LocalBottomSheetNavigator

class GenerateDocsGenerationDialogScreen(
    val tableData: List<List<String>>,
    val pathToTemplate: String,
    val pathToDestination: String,
    val ignoreLastNColumn: Int,
    val unionLastNColumn: Int,
) : Screen {
    @Composable
    override fun Content() {
        val screenModel = koinScreenModel<GenerateDocsGenerationDialogStateModel>()
        val state by screenModel.state.collectAsStateWithLifecycle()
        val bottomSheetNavigator = LocalBottomSheetNavigator.current

        LaunchedEffect(Unit) {
            screenModel.onIntent(GenerateDocsGenerationDialogIntent.StartGeneration(
                tableData,
                pathToTemplate,
                pathToDestination,
                ignoreLastNColumn,
                unionLastNColumn
            ))
        }

        LaunchedEffect(screenModel) {
            screenModel.effect.collect {
                when (it) {
                    GenerateDocsGenerationDialogEffect.DismissDialog -> bottomSheetNavigator.hide()
                }
            }
        }

        GenerateDocsGenerationDialogContent(state, onIntent = screenModel::onIntent)
    }
}