package ru.jengle88.klarkclient.domain.usecase.auth

import ru.jengle88.klarkclient.domain.api.auth.AuthStore

class LogoutUseCase(
    private val authStore: AuthStore,
) {
    operator fun invoke() {
        authStore.clearAuth()
    }
}