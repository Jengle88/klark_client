package ru.jengle88.klarkclient.domain.api.auth

/**
 * Interface responsible for intercepting the authorization code during an OAuth2 authentication process.
 *
 * Implementations of this interface typically manage a temporary local HTTP server that waits
 * for a redirect from the identity provider. This allows the application to programmatically
 * receive the authorization code required to complete the token exchange.
 */
interface AuthCodeReceiver {
    /**
     * Suspends until an authorization code is captured from a local redirect.
     *
     * Starts a temporary server to listen for the incoming OAuth2 redirect. The server
     * checks that the state parameter in the request matches the expected value to
     * prevent cross-site request forgery.
     *
     * @param port The network port where the local server should listen.
     * @param expectedState The state value to compare against the received request parameter.
     * @param onServerReady Callback invoked once the server is active and ready for the redirect.
     * @return The captured authorization code if successful, or null if the operation fails.
     */
    suspend fun awaitAuthCode(port: Int, expectedState: String, onServerReady: (port: Int) -> Unit): String?
}