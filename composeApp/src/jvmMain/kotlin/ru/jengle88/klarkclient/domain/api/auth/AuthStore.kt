package ru.jengle88.klarkclient.domain.api.auth

import kotlinx.coroutines.flow.StateFlow
import ru.jengle88.klarkclient.data.network.dto.UserProfile

/**
 * Abstraction for managing authentication state within the application.
 *
 * Implementations of this interface are responsible for holding the current access token
 * and associated [UserProfile] (if any) and exposing them as observable [kotlinx.coroutines.flow.StateFlow]s.
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
    fun saveAuth(token: String, refreshToken: String?, userProfile: UserProfile?)

    /**
     * Clear all stored authentication data, effectively logging out the user.
     *
     * After this call, [accessToken] and [userProfile] should both emit `null`.
     */
    fun clearAuth()
}
