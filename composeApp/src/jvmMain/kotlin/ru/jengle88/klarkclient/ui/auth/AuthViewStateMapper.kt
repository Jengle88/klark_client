package ru.jengle88.klarkclient.ui.auth

fun AuthUserState.toViewState(
    isLoading: Boolean = false,
    error: String? = null,
): AuthViewState =
    AuthViewState(
        isAuthorized = isAuthorized,
        initials = initials,
        isLoading = isLoading,
        error = error,
    )
