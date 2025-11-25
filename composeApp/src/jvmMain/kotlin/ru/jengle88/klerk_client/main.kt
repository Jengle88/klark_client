package ru.jengle88.klerk_client

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import cafe.adriel.voyager.navigator.Navigator

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "klerk_client",
    ) {
        Navigator(App()) {

        }
    }
}