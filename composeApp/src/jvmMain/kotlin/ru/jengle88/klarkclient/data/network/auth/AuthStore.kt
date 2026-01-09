package ru.jengle88.klarkclient.data.network.auth

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

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
     * A reactive stream of the current authenticated user's profile.
     *
     * The value is `null` when no user is authenticated or when no profile data is available.
     */
    val userProfile: StateFlow<UserProfile?>

    /**
     * Persist the provided authentication data.
     *
     * @param token the access token associated with the authenticated session.
     * @param userProfile optional profile information for the authenticated user.
     */
    fun saveAuth(token: String, userProfile: UserProfile?)

    /**
     * Clear all stored authentication data, effectively logging out the user.
     *
     * After this call, [accessToken] and [userProfile] should both emit `null`.
     */
    fun clearAuth()
}

class AuthStoreImpl : AuthStore {
    private val _accessToken = MutableStateFlow<String?>(null)
    override val accessToken: StateFlow<String?> = _accessToken.asStateFlow()

    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    override val userProfile: StateFlow<UserProfile?> = _userProfile.asStateFlow()

    override fun saveAuth(token: String, userProfile: UserProfile?) {
        _accessToken.value = token
        _userProfile.value = userProfile
    }

    override fun clearAuth() {
        _accessToken.value = null
        _userProfile.value = null
    }
}
