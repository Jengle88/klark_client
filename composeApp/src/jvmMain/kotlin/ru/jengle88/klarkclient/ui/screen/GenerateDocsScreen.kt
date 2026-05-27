package ru.jengle88.klarkclient.ui.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.bottomSheet.LocalBottomSheetNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import io.github.vinceglb.filekit.compose.rememberDirectoryPickerLauncher
import io.github.vinceglb.filekit.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.core.PickerMode
import io.github.vinceglb.filekit.core.PickerType
import kotlinx.coroutines.flow.collectLatest
import ru.jengle88.klarkclient.ui.generatedocs.GenerateDocsContent
import ru.jengle88.klarkclient.ui.generatedocs.GenerateDocsEffect
import ru.jengle88.klarkclient.ui.generatedocs.GenerateDocsIntent
import ru.jengle88.klarkclient.ui.generatedocs.GenerateDocsStateModel
import ru.jengle88.klarkclient.ui.generatedocs.generationdialog.GenerateDocsGenerationDialogScreen

@Immutable
class GenerateDocsScreen : Screen {
    @Composable
    override fun Content() {
        val screenModel = koinScreenModel<GenerateDocsStateModel>()
        val state by screenModel.state.collectAsState()
        val navigator = LocalNavigator.currentOrThrow
        val bottomSheetNavigator = LocalBottomSheetNavigator.current

        GenerateDocsContent(
            state,
            onIntent = screenModel::onIntent,
            onEffect = screenModel::onEffect,
            onBack = navigator::pop
        )

        val tablePickerLauncher =
            rememberFilePickerLauncher(
                type = PickerType.File(extensions = screenModel.supportedTableFormat),
                mode = PickerMode.Single,
                title = "Выберите таблицу с данными для генерации",
            ) { file ->
                file?.path?.let { path ->
                    screenModel.onIntent(GenerateDocsIntent.UpdatePathToTable(path))
                }
            }

        val templatePickerLauncher =
            rememberDirectoryPickerLauncher(
                title = "Выберите папку с шаблонами для использования",
            ) { directory ->
                directory?.path?.let { path ->
                    screenModel.onIntent(GenerateDocsIntent.UpdatePathToTemplate(path))
                }
            }

        val destinationPickerLauncher =
            rememberDirectoryPickerLauncher(
                title = "Выберите папку для сохранения результатов",
            ) { directory ->
                directory?.path?.let { path ->
                    screenModel.onIntent(GenerateDocsIntent.UpdatePathToDestination(path))
                }
            }

        LaunchedEffect(screenModel) {
            screenModel.effect.collectLatest { effect ->
                when (effect) {
                    GenerateDocsEffect.ShowTablePicker -> tablePickerLauncher.launch()
                    GenerateDocsEffect.ShowTemplatePicker -> templatePickerLauncher.launch()
                    GenerateDocsEffect.ShowDestinationPicker -> destinationPickerLauncher.launch()
                    is GenerateDocsEffect.ShowProcessingBottomSheet ->
                        bottomSheetNavigator.show(
                            GenerateDocsGenerationDialogScreen(
                                effect.tableData,
                                effect.pathToTemplate,
                                effect.pathToDestination,
                                effect.ignoreLastNColumn,
                                effect.unionLastNColumn,
                            ),
                        )
                }
            }
        }
    }
}