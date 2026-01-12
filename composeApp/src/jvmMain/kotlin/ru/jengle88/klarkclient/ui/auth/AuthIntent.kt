package ru.jengle88.klarkclient.ui.auth

sealed interface AuthIntent {
    data object Login : AuthIntent
    data object Logout : AuthIntent
}
