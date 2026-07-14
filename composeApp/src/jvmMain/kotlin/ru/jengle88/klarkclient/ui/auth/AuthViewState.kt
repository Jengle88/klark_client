package ru.jengle88.klarkclient.ui.auth

data class AuthViewState(
    val isAuthorized: Boolean = false,
    val initials: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
)
