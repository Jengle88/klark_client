package ru.jengle88.klarkclient.ui

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.CurrentScreen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.bottomSheet.BottomSheetNavigator
import cafe.adriel.voyager.navigator.bottomSheet.LocalBottomSheetNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import io.github.vinceglb.filekit.compose.rememberDirectoryPickerLauncher
import io.github.vinceglb.filekit.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.core.PickerMode
import io.github.vinceglb.filekit.core.PickerType
import kotlinx.coroutines.flow.collectLatest
import ru.jengle88.klarkclient.ui.datamodels.AppScreenDestination
import ru.jengle88.klarkclient.ui.generatedocs.GenerateDocsContent
import ru.jengle88.klarkclient.ui.generatedocs.GenerateDocsEffect
import ru.jengle88.klarkclient.ui.generatedocs.GenerateDocsIntent
import ru.jengle88.klarkclient.ui.generatedocs.GenerateDocsStateModel
import ru.jengle88.klarkclient.ui.generatedocs.generationdialog.GenerateDocsGenerationDialogScreen
import ru.jengle88.klarkclient.ui.maintab.MainTabContent
import ru.jengle88.klarkclient.ui.maintab.MainTabScreenModel

@Immutable
object MainTab : Tab {
    override val options: TabOptions
        @Composable
        get() {
            val title = "Главная"
            val icon = rememberVectorPainter(Icons.Default.Home)

            return remember {
                TabOptions(
                    index = 0u,
                    title = title,
                    icon = icon,
                )
            }
        }

    @OptIn(ExperimentalMaterialApi::class)
    @Composable
    override fun Content() {
        BottomSheetNavigator {
            Navigator(MainTabScreen()) {
                CurrentScreen()
            }
        }
    }
}

@Immutable
class MainTabScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val screenModel = koinScreenModel<MainTabScreenModel>()
        val features by screenModel.state.collectAsStateWithLifecycle()

        MainTabContent(features, onNavigate = { route ->
            when (route) {
                AppScreenDestination.GENERATION -> navigator.push(GenerateDocsScreen())
                else -> TODO()
            }
        })
    }
}

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
            onBack = { navigator.pop() }
        )

        val tablePickerLauncher =
            rememberFilePickerLauncher(
                type = PickerType.File(extensions = listOf("xlsx", "xls")),
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
