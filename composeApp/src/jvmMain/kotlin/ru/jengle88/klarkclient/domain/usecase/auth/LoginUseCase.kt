package ru.jengle88.klarkclient.domain.usecase.auth

import kotlinx.coroutines.withContext
import ru.jengle88.klarkclient.common.CoroutineDispatchers
import ru.jengle88.klarkclient.data.network.auth.AuthManager
import ru.jengle88.klarkclient.data.network.auth.AuthProvider
import ru.jengle88.klarkclient.data.network.auth.AuthStore
import ru.jengle88.klarkclient.data.network.auth.UserProfile

class LoginUseCase(
    private val authManager: AuthManager,
    private val authProvider: AuthProvider,
    private val authStore: AuthStore,
    private val coroutineDispatcher: CoroutineDispatchers,
) {
    @Throws(IllegalStateException::class)
    suspend operator fun invoke(): UserProfile {
        val tokens = authManager.login(authProvider)
        val userProfile = withContext(coroutineDispatcher.io) {
            authManager.getUserProfile(tokens.accessToken, authProvider)
        }
        authStore.saveAuth(tokens.accessToken, tokens.refreshToken, userProfile)
        return userProfile
    }
}