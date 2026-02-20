package ru.jengle88.klarkclient.ui.app

import androidx.compose.foundation.layout.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.tab.CurrentTab
import cafe.adriel.voyager.navigator.tab.TabNavigator
import org.koin.compose.koinInject
import ru.jengle88.klarkclient.ui.auth.AuthIntent
import ru.jengle88.klarkclient.ui.auth.AuthStatusRectangleAvatar
import ru.jengle88.klarkclient.ui.auth.AuthViewScreenModel
import ru.jengle88.klarkclient.ui.maintab.MainTabNavigationRailItem
import ru.jengle88.klarkclient.ui.screen.main.MainTab

@Composable
fun App() {
    val authScreenModel: AuthViewScreenModel = koinInject()
    val authState by authScreenModel.state.collectAsState()

    TabNavigator(MainTab) {
        Row(modifier = Modifier.fillMaxSize()) {
            NavigationRail(
                modifier = Modifier.fillMaxHeight(),
                header = {
                    Text(
                        text = "Klark",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    AuthStatusRectangleAvatar(
                        isAuthorized = authState.isAuthorized,
                        userName = authState.initials,
                        isLoading = authState.isLoading,
                        onClick = {
                            if (!authState.isAuthorized) {
                                authScreenModel.onIntent(AuthIntent.Login)
                            } else {
                                authScreenModel.onIntent(AuthIntent.Logout)
                            }
                        }
                    )
                },
            ) {
                TabList(Modifier)
            }
            HorizontalDivider(
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
            )
            Box(modifier = Modifier.weight(1f)) {
                CurrentTab()
            }
        }
    }
}

@Composable
private fun TabList(modifier: Modifier = Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        MainTabNavigationRailItem(MainTab)
    }
}
