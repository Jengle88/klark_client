package ru.jengle88.klarkclient.ui.maintab

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.tab.LocalTabNavigator
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabNavigator
import org.jetbrains.compose.ui.tooling.preview.Preview
import ru.jengle88.klarkclient.ui.screen.MainTab

@Composable
fun MainTabNavigationRailItem(tab: Tab) {
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
                style = MaterialTheme.typography.labelSmall,
            )
        },
        colors =
        NavigationRailItemDefaults.colors(
            selectedIconColor = MaterialTheme.colorScheme.primary,
            unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            selectedTextColor = MaterialTheme.colorScheme.primary,
            unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
        ),
    )
}

@Preview
@Composable
private fun PreviewMainTabNavigationRailItem() {
    MaterialTheme {
        NavigationRail {
            Column {
                TabNavigator(MainTab) {
                    MainTabNavigationRailItem(tab = MainTab)
                }
            }
        }
    }
}
