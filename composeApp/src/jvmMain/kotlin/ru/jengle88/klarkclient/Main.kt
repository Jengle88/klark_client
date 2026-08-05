package ru.jengle88.klarkclient

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import org.koin.core.context.startKoin
import ru.jengle88.klarkclient.di.allModules
import ru.jengle88.klarkclient.ui.App

fun main() = application {
    startKoin {
        modules(allModules)
    }

    MaterialTheme {
        Window(
            onCloseRequest = ::exitApplication,
            title = "Klark",
        ) {
            App()
        }
    }
}
