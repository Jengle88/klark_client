package ru.jengle88.klarkclient.data.network.auth

import androidx.annotation.WorkerThread
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import ru.jengle88.klarkclient.common.UrlLauncher
import ru.jengle88.klarkclient.di.AuthHttpClient
import java.util.UUID
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

        val state = UUID.randomUUID().toString() // генерируем для безопасности, чтобы сравнивать при возвращении запроса
        val url = provider.getAuthorizeUrl(redirectUri, state)
        val code = authCodeReceiver.awaitAuthCode(port, expectedState = state, onServerReady = {
            openBrowser(url)
        })
        checkNotNull(code) { "Auth code is null" }
        return@withContext provider.exchangeCodeForToken(authHttpClient.httpClient, code, redirectUri)
    }

    @WorkerThread
    suspend fun getUserProfile(accessToken: String, provider: AuthProvider): UserProfile {
        val userProfile = provider.getUserProfile(authHttpClient.httpClient, accessToken)
        return userProfile
    }


    private fun openBrowser(url: String) {
        uriLauncher.open(url)
    }
}