package ru.jengle88.klarkclient.ui.auth

data class AuthState(
    val isAuthorized: Boolean = false,
    val userNameInitials: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)
