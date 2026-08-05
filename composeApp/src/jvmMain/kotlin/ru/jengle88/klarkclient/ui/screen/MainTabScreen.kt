package ru.jengle88.klarkclient.ui.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import ru.jengle88.klarkclient.ui.datamodels.AppScreenDestination
import ru.jengle88.klarkclient.ui.maintab.MainTabContent
import ru.jengle88.klarkclient.ui.maintab.MainTabScreenModel

@Immutable
class MainTabScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val screenModel = koinScreenModel<MainTabScreenModel>()
        val features by screenModel.state.collectAsStateWithLifecycle()
        val updateInfo by screenModel.updateInfo.collectAsStateWithLifecycle()

        MainTabContent(
            features = features,
            updateInfo = updateInfo,
            onNavigate = { route ->
                when (route) {
                    AppScreenDestination.GENERATION -> navigator.push(GenerateDocsScreen())
                    else -> TODO()
                }
            },
            onUpdateClick = screenModel::onUpdateClick,
        )
    }
}
