package ru.jengle88.klarkclient.domain.usecase.auth

import kotlinx.coroutines.withContext
import ru.jengle88.klarkclient.common.CoroutineDispatchers
import ru.jengle88.klarkclient.domain.api.auth.AuthManager
import ru.jengle88.klarkclient.domain.api.auth.AuthStore

class LoginUseCase(
    private val authManager: AuthManager,
    private val authStore: AuthStore,
    private val coroutineDispatcher: CoroutineDispatchers
) {
    @Throws(IllegalStateException::class)
    suspend operator fun invoke() {
        val tokens = authManager.login()
        val userProfile =
            withContext(coroutineDispatcher.io) {
                authManager.getUserProfile(tokens.accessToken)
            }
        authStore.saveAuth(tokens.accessToken, tokens.refreshToken, userProfile)
    }
}
