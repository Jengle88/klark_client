package ru.jengle88.klarkclient.data.network.auth

import io.ktor.client.HttpClient

/**
 * Abstraction over an OAuth authentication provider.
 *
 * Implementations of this interface encapsulate provider-specific details such as
 * authorization endpoints, token endpoints, scopes and user info APIs, while exposing
 * a common high-level flow to the rest of the application:
 *
 * 1. Build an authorization URL via [getAuthorizeUrl] and direct the user to it.
 * 2. After the user authorizes the application, the provider redirects back with an
 *    authorization code to the given redirect URI.
 * 3. Exchange the authorization code for access/refresh tokens via [exchangeCodeForToken].
 * 4. Use the access token to request the user's profile via [getUserProfile].
 *
 * Implementations must be safe to call from coroutines and are responsible for:
 * - Correctly constructing provider-specific URLs and parameters.
 * - Handling any necessary request/response mappings.
 * - Throwing appropriate exceptions when the remote service returns errors.
 */
interface AuthProvider {
    /**
     * Builds a provider-specific URL that initiates the OAuth authorization flow.
     *
     * The user should be redirected to the returned URL. After a successful authorization,
     * the provider is expected to redirect the user back to [redirectUri] with an
     * authorization code that can be exchanged via [exchangeCodeForToken].
     *
     * @param redirectUri The redirect URI registered for this client with the provider.
     * @param state A random string used to protect against CSRF attacks.
     * @return A complete URL that can be opened in a browser to start the OAuth flow.
     */
    fun getAuthorizeUrl(
        redirectUri: String,
        state: String,
    ): String

    /**
     * Exchanges an authorization code for OAuth tokens.
     *
     * This corresponds to the "authorization code" grant step where the backend calls
     * the provider's token endpoint, passing the [code] received at [redirectUri].
     *
     * @param httpClient The [HttpClient] instance used to perform network requests.
     * @param code The authorization code returned by the provider after user authorization.
     * @param redirectUri The same redirect URI that was used in [getAuthorizeUrl].
     * @return An [AuthTokens] instance containing at least an access token and optionally a refresh token.
     */
    suspend fun exchangeCodeForToken(
        httpClient: HttpClient,
        code: String,
        redirectUri: String,
    ): AuthTokens

    /**
     * Refreshes the OAuth tokens using the provided refresh token.
     *
     * This method is used to get a new access token when the current one expires,
     * by making a request to the OAuth provider's token endpoint.
     *
     * @param httpClient The HttpClient instance used to perform the network request.
     * @param refreshToken The refresh token used to obtain a new access token.
     * @return An AuthTokens instance containing the new access token, and optionally a new refresh token and expiration time.
     */
    suspend fun refreshToken(
        httpClient: HttpClient,
        refreshToken: String,
    ): AuthTokens

    /**
     * Retrieves the authenticated user's profile using a valid access token.
     *
     * Implementations should call the provider's user info endpoint (or equivalent),
     * using [accessToken] for authorization, and map the response into a [UserProfile].
     *
     * @param httpClient The [HttpClient] instance used to perform network requests.
     * @param accessToken The access token obtained from [exchangeCodeForToken].
     * @return A [UserProfile] representing the authenticated user.
     */
    suspend fun getUserProfile(
        httpClient: HttpClient,
        accessToken: String,
    ): UserProfile
}
