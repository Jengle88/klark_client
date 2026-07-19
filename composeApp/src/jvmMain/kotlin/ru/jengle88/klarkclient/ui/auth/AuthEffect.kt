package ru.jengle88.klarkclient.ui.auth

sealed interface AuthEffect {
    data class Error(
        val message: String,
    ) : AuthEffect
}
