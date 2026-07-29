package ru.jengle88.klarkclient.data.network.auth

import androidx.annotation.WorkerThread
import java.util.UUID
import kotlin.jvm.Throws
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import ru.jengle88.klarkclient.common.UrlLauncher
import ru.jengle88.klarkclient.data.network.dto.AuthTokens
import ru.jengle88.klarkclient.data.network.dto.UserProfile
import ru.jengle88.klarkclient.domain.api.auth.AuthCodeReceiver
import ru.jengle88.klarkclient.domain.api.auth.AuthManager
import ru.jengle88.klarkclient.domain.api.auth.AuthProvider

class AuthManagerImpl(
    private val authHttpClient: AuthHttpClient,
    private val authProvider: AuthProvider,
    private val uriLauncher: UrlLauncher,
    private val authCodeReceiver: AuthCodeReceiver,
    private val port: Int,
    private val ioDispatcher: CoroutineDispatcher
) : AuthManager {
    @Throws(IllegalStateException::class)
    override suspend fun login(): AuthTokens = withContext(ioDispatcher) {
        // генерируем для безопасности, чтобы сравнивать при возвращении запроса
        val state = UUID.randomUUID().toString()

        // redirectUri is constructed after we know the actual port
        var redirectUri = ""

        val code =
            authCodeReceiver.awaitAuthCode(
                port,
                expectedState = state,
                onServerReady = { actualPort ->
                    redirectUri = "http://localhost:$actualPort"
                    val url = authProvider.getAuthorizeUrl(redirectUri, state)
                    openBrowser(url)
                }
            )
        checkNotNull(code) { "Auth code is null" }
        return@withContext authProvider.exchangeCodeForToken(
            authHttpClient.httpClient,
            code,
            redirectUri
        )
    }

    @WorkerThread
    override suspend fun getUserProfile(accessToken: String): UserProfile {
        val userProfile = authProvider.getUserProfile(authHttpClient.httpClient, accessToken)
        return userProfile
    }

    private fun openBrowser(url: String) {
        uriLauncher.open(url)
    }
}
