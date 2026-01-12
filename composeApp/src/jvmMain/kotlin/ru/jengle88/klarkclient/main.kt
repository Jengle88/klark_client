package ru.jengle88.klarkclient

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import cafe.adriel.voyager.navigator.tab.CurrentTab
import cafe.adriel.voyager.navigator.tab.LocalTabNavigator
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabNavigator
import org.koin.compose.koinInject
import org.koin.core.context.startKoin
import ru.jengle88.klarkclient.di.appModule
import ru.jengle88.klarkclient.di.screenModelModule
import ru.jengle88.klarkclient.ui.MainTab
import ru.jengle88.klarkclient.ui.auth.AuthIntent
import ru.jengle88.klarkclient.ui.auth.AuthScreenModel
import ru.jengle88.klarkclient.ui.components.AuthStatusCircleAvatar

fun main() =
    application {
        startKoin {
            modules(appModule, screenModelModule)
        }

        Window(
            onCloseRequest = ::exitApplication,
            title = "Klark",
        ) {
            App()
        }
    }

@Composable
fun App() {
    val authScreenModel: AuthScreenModel = koinInject()
    val authState by authScreenModel.state.collectAsState()

    TabNavigator(MainTab) {
        Row(modifier = Modifier.fillMaxSize()) {
            NavigationRail(
                modifier = Modifier.fillMaxHeight(),
                elevation = 2.dp,
                header = {
                    Text(
                        text = "Klark",
                        style = MaterialTheme.typography.h5,
                        color = MaterialTheme.colors.primary,
                    )
                },
            ) {
                AuthStatusCircleAvatar(
                    isAuthorized = authState.isAuthorized,
                    userName = authState.userNameInitials,
                    isLoading = authState.isLoading,
                    onClick = {
                        if (!authState.isAuthorized) {
                            authScreenModel.onIntent(AuthIntent.Login)
                        } else {
                            authScreenModel.onIntent(AuthIntent.Logout)
                        }
                    },
                )
                TabNavigationRailItem(MainTab)
            }
            Divider(
                modifier = Modifier.fillMaxHeight().width(1.dp),
                color = MaterialTheme.colors.onSurface.copy(alpha = 0.12f),
            )

            Box(modifier = Modifier.weight(1f)) {
                CurrentTab()
            }
        }
    }
}

@Composable
fun TabNavigationRailItem(tab: Tab) {
    val tabNavigator = LocalTabNavigator.current
    val isSelected = tabNavigator.current == tab

    NavigationRailItem(
        selected = isSelected,
        onClick = { tabNavigator.current = tab },
        icon = {
            Icon(
                modifier = Modifier.size(24.dp),
                painter = tab.options.icon!!,
                contentDescription = tab.options.title,
            )
        },
        label = {
            Text(
                text = tab.options.title,
                style = MaterialTheme.typography.caption,
            )
        },
        alwaysShowLabel = true,
        selectedContentColor = MaterialTheme.colors.primary,
        unselectedContentColor = MaterialTheme.colors.onSurface.copy(alpha = 0.6f),
    )
}
