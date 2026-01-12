package ru.jengle88.klarkclient.data.network.auth

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import ru.jengle88.klarkclient.data.security.SecureStorage
import ru.jengle88.klarkclient.data.security.SecureStorageFactory

/**
 * Abstraction for managing authentication state within the application.
 *
 * Implementations of this interface are responsible for holding the current access token
 * and associated [UserProfile] (if any) and exposing them as observable [StateFlow]s.
 *
 * Typical usage:
 * - Read [accessToken] and [userProfile] to reactively update UI or network clients
 *   when authentication state changes.
 * - Call [saveAuth] after a successful login to store the new credentials.
 * - Call [clearAuth] to log out the user and clear all authentication-related data.
 */
interface AuthStore {
    /**
     * A reactive stream of the current access token.
     *
     * The value is `null` when no user is authenticated.
     */
    val accessToken: StateFlow<String?>

    /**
     * A reactive stream of the current refresh token.
     *
     * The refresh token is used to get a new access token when the existing one expires.
     * The value is `null` when no user is authenticated or when the refresh token is unavailable.
     */
    val refreshToken: StateFlow<String?>

    /**
     * A reactive stream of the current authenticated user's profile.
     *
     * The value is `null` when no user is authenticated or when no profile data is available.
     */
    val userProfile: StateFlow<UserProfile?>

    /**
     * Persist the provided authentication data.
     *
     * @param token the access token associated with the authenticated session.
     * @param refreshToken the refresh token used to get new access tokens.
     * @param userProfile optional profile information for the authenticated user.
     */
    fun saveAuth(
        token: String,
        refreshToken: String?,
        userProfile: UserProfile?,
    )

    /**
     * Clear all stored authentication data, effectively logging out the user.
     *
     * After this call, [accessToken] and [userProfile] should both emit `null`.
     */
    fun clearAuth()
}

class AuthStoreImpl(
    private val secureStorage: SecureStorage = SecureStorageFactory.create(),
) : AuthStore {
    private val _accessToken = MutableStateFlow<String?>(secureStorage.retrieve(KEY_ACCESS_TOKEN))
    override val accessToken: StateFlow<String?> = _accessToken.asStateFlow()

    private val _refreshToken = MutableStateFlow<String?>(secureStorage.retrieve(KEY_REFRESH_TOKEN))
    override val refreshToken: StateFlow<String?> = _refreshToken.asStateFlow()

    private val _userProfile = MutableStateFlow(loadUserProfile())
    override val userProfile: StateFlow<UserProfile?> = _userProfile.asStateFlow()

    override fun saveAuth(
        token: String,
        refreshToken: String?,
        userProfile: UserProfile?,
    ) {
        _accessToken.value = token
        _refreshToken.value = refreshToken
        _userProfile.value = userProfile

        secureStorage.store(KEY_ACCESS_TOKEN, token)
        if (refreshToken != null) {
            secureStorage.store(KEY_REFRESH_TOKEN, refreshToken)
        }
        if (userProfile != null) {
            try {
                val profileJson = Json.encodeToString(userProfile)
                secureStorage.store(KEY_USER_PROFILE, profileJson)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun clearAuth() {
        _accessToken.value = null
        _refreshToken.value = null
        _userProfile.value = null

        secureStorage.remove(KEY_ACCESS_TOKEN)
        secureStorage.remove(KEY_REFRESH_TOKEN)
        secureStorage.remove(KEY_USER_PROFILE)
    }

    private fun loadUserProfile(): UserProfile? {
        val jsonString = secureStorage.retrieve(KEY_USER_PROFILE) ?: return null
        return try {
            Json.decodeFromString(jsonString)
        } catch (e: Exception) {
            null
        }
    }

    private companion object {
        private const val KEY_ACCESS_TOKEN = "auth_access_token"
        const val KEY_REFRESH_TOKEN = "auth_refresh_token"
        private const val KEY_USER_PROFILE = "auth_user_profile"
    }
}
