package ru.jengle88.klarkclient.data.network.auth

import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.cio.CIO
import io.ktor.server.engine.embeddedServer
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import ru.jengle88.klarkclient.common.UrlLauncher
import ru.jengle88.klarkclient.di.AuthHttpClient
import kotlin.jvm.Throws

private const val DEFAULT_AUTH_SERVER_PORT = 2538

class AuthManager(
    private val authHttpClient: AuthHttpClient,
    private val uriLauncher: UrlLauncher,
    private val authCodeReceiver: AuthCodeReceiver,
    private val port: Int = DEFAULT_AUTH_SERVER_PORT,
    private val ioDispatcher: CoroutineDispatcher,
) {
    @Throws(IllegalStateException::class)
    suspend fun login(provider: AuthProvider): AuthTokens = withContext(ioDispatcher) {
        val redirectUri = "http://localhost:$port"

        val url = provider.getAuthorizeUrl(redirectUri)
        val code = authCodeReceiver.awaitAuthCode(port, onServerReady = {
            openBrowser(url)
        })
        checkNotNull(code) { "Auth code is null" }
        return@withContext provider.exchangeCodeForToken(authHttpClient.httpClient, code, redirectUri)
    }

    suspend fun getUserProfile(accessToken: String, provider: AuthProvider): UserProfile = withContext(ioDispatcher) {
        val userProfile = provider.getUserProfile(authHttpClient.httpClient, accessToken)
        return@withContext userProfile
    }


    private fun openBrowser(url: String) {
        uriLauncher.open(url)
    }
}