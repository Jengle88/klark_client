package ru.jengle88.klarkclient.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.tab.CurrentTab
import cafe.adriel.voyager.navigator.tab.TabNavigator
import ru.jengle88.klarkclient.ui.maintab.MainTabNavigationRailItem
import ru.jengle88.klarkclient.ui.screen.MainTab

@Composable
fun App() {
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
                    // Auth feature is disabled in UI but kept in code.
                    // See AuthViewScreenModel and AuthStatusRectangleAvatar to re-enable.
                },
            ) {
                TabList(Modifier)
            }
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
