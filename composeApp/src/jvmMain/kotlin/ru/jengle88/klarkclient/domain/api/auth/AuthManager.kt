package ru.jengle88.klarkclient.domain.api.auth

import androidx.annotation.WorkerThread
import ru.jengle88.klarkclient.data.network.dto.AuthTokens
import ru.jengle88.klarkclient.data.network.dto.UserProfile
import kotlin.jvm.Throws

/**
 * Entry point for managing authentication flows and retrieving user identity information.
 * This interface abstracts the complexity of interacting with different identity providers.
 */
interface AuthManager {
    /**
     * Initiates the authentication flow to get user credentials.
     *
     * This method coordinates the multistep OAuth2 process, typically involving user authorization
     * and the later exchange of an authorization code for security tokens. The function
     * suspends until the tokens are successfully retrieved or an error occurs.
     *
     * @return The authentication tokens containing access and refresh credentials.
     * @throws IllegalStateException If the authentication process fails, is canceled, or encounters an unexpected state.
     */
    @Throws(IllegalStateException::class)
    suspend fun login(): AuthTokens

    @WorkerThread
    suspend fun getUserProfile(accessToken: String): UserProfile
}