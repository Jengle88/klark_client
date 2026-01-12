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
import ru.jengle88.klarkclient.data.network.auth.AuthStore
import ru.jengle88.klarkclient.data.network.auth.YandexAuthProvider

class AiHttpClient(
    authStore: AuthStore,
    authProvider: YandexAuthProvider,
    authHttpClient: AuthHttpClient,
) {
    val httpClient by lazy {
        HttpClient(CIO) {
            install(ContentNegotiation) {
                json(
                    Json {
                        prettyPrint = true
                        isLenient = true
                        ignoreUnknownKeys = true
                    },
                )
            }

            install(Auth) {
                bearer {
                    // 1. Откуда брать токен для обычных запросов
                    loadTokens {
                        val accessToken = authStore.accessToken.value
                        val refreshToken = authStore.refreshToken.value
                        if (accessToken != null && refreshToken != null) {
                            BearerTokens(accessToken, refreshToken)
                        } else {
                            null
                        }
                    }

                    // 2. Что делать, если пришел 401 Unauthorized
                    refreshTokens {
                        val oldTokens = oldTokens // Старые токены доступны здесь
                        val refreshToken = oldTokens?.refreshToken ?: return@refreshTokens null

                        try {
                            // Выполняем запрос на обновление
                            val newTokens = authProvider.refreshToken(authHttpClient.httpClient, refreshToken)

                            // Сохраняем новые токены в Store (чтобы они записались в файл и память)
                            authStore.saveAuth(
                                newTokens.accessToken,
                                newTokens.refreshToken,
                                authStore.userProfile.value,
                            )

                            // Возвращаем их плагину, чтобы он повторил упавший запрос
                            BearerTokens(newTokens.accessToken, newTokens.refreshToken ?: refreshToken)
                        } catch (e: Exception) {
                            // Если обновить не удалось (например, refresh token тоже протух)
                            // Логируем и разлогиниваем пользователя
                            authStore.clearAuth()
                            null
                        }
                    }
                }
            }
        }
    }
}
