package ru.jengle88.klarkclient.data.network.ai

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import ru.jengle88.klarkclient.data.network.auth.AuthHttpClient
import ru.jengle88.klarkclient.data.network.auth.YandexAuthProvider
import ru.jengle88.klarkclient.domain.api.auth.AuthStore

class AiHttpClient(
    authStore: AuthStore,
    authProvider: YandexAuthProvider,
    authHttpClient: AuthHttpClient
) {
    val httpClient by lazy {
        HttpClient(CIO) {
            install(ContentNegotiation) {
                json(
                    Json {
                        prettyPrint = true
                        isLenient = true
                        ignoreUnknownKeys = true
                    }
                )
            }

            install(Auth) {
                bearer {
                    // Откуда брать токен для обычных запросов
                    loadTokens {
                        val accessToken = authStore.accessToken.value
                        val refreshToken = authStore.refreshToken.value
                        if (accessToken != null && refreshToken != null) {
                            BearerTokens(accessToken, refreshToken)
                        } else {
                            null
                        }
                    }

                    // Что делать, если пришел 401 Unauthorized
                    refreshTokens {
                        val oldTokens = oldTokens
                        val refreshToken = oldTokens?.refreshToken ?: return@refreshTokens null

                        try {
                            val newTokens = authProvider.refreshToken(
                                authHttpClient.httpClient,
                                refreshToken
                            )

                            authStore.saveAuth(
                                newTokens.accessToken,
                                newTokens.refreshToken,
                                authStore.userProfile.value
                            )

                            BearerTokens(newTokens.accessToken, newTokens.refreshToken)
                        } catch (e: Exception) {
                            authStore.clearAuth()
                            null
                        }
                    }
                }
            }
        }
    }
}
