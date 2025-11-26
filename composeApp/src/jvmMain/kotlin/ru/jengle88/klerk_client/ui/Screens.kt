package ru.jengle88.klerk_client.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.CurrentScreen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import ru.jengle88.klerk_client.ui.datamodels.AppScreenDestination
import ru.jengle88.klerk_client.ui.generatedocs.GenerateDocsContent
import ru.jengle88.klerk_client.ui.generatedocs.GenerateDocsStateModel
import ru.jengle88.klerk_client.ui.statemodels.MainTabScreenModel


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
                    icon = icon
                )
            }
        }

    @Composable
    override fun Content() {
        Navigator(MainTabScreen()) {
            CurrentScreen()
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

        MainTabContent(features, { route ->
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
        val screenModel = rememberScreenModel { GenerateDocsStateModel() }
        val state by screenModel.state.collectAsState()
        val navigator = LocalNavigator.currentOrThrow

        GenerateDocsContent(state, screenModel::onIntent, { navigator.pop() })
    }
}
