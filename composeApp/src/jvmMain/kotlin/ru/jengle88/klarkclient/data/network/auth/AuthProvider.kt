package ru.jengle88.klarkclient.data.network.auth

import io.ktor.client.HttpClient

interface AuthProvider {

    // Ссылка для авторизации
    fun getAuthorizeUrl(redirectUri: String): String

    // Получаем токен
    suspend fun exchangeCodeForToken(httpClient: HttpClient, code: String, redirectUri: String): AuthTokens

    suspend fun getUserProfile(httpClient: HttpClient, accessToken: String): UserProfile
}