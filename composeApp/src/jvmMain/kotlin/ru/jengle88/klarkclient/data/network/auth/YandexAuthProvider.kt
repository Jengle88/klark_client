package ru.jengle88.klarkclient.data.network.auth

import io.ktor.client.HttpClient
import io.ktor.client.call.DoubleReceiveException
import io.ktor.client.call.NoTransformationFoundException
import io.ktor.client.call.body
import io.ktor.client.request.forms.submitForm
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.http.parameters
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.jvm.Throws

class YandexAuthProvider(
    private val clientId: String,
    private val clientSecret: String,
) : AuthProvider {

    override fun getAuthorizeUrl(redirectUri: String, state: String): String {
        return "https://oauth.yandex.ru/authorize?response_type=code&client_id=$clientId&redirect_uri=$redirectUri&state=$state"
    }

    @Throws(DoubleReceiveException::class, NoTransformationFoundException::class)
    override suspend fun exchangeCodeForToken(
        httpClient: HttpClient,
        code: String,
        redirectUri: String
    ): AuthTokens {
        val response: YandexOAuthResponse = httpClient.submitForm(
            url = "https://oauth.yandex.ru/token",
            formParameters = parameters {
                append("grant_type", "authorization_code")
                append("code", code)
                append("client_id", clientId)
                append("client_secret", clientSecret)
                append("redirect_uri", redirectUri)
            }
        ).body()

        return AuthTokens(accessToken = response.accessToken, expiresIn = response.expiresIn)
    }

    @Throws(DoubleReceiveException::class, NoTransformationFoundException::class)
    override suspend fun getUserProfile(
        httpClient: HttpClient,
        accessToken: String
    ): UserProfile {
        val rawProfile: YandexUserProfile = httpClient.get("https://login.yandex.ru/info") {
            header("Authorization", "OAuth $accessToken")
            parameter("format", "json")
        }.body()

        return UserProfile(
            firstName = rawProfile.firstName ?: "",
            lastName = rawProfile.lastName ?: ""
        )
    }

    // Внутренняя DTO для парсинга
    @Serializable
    private data class YandexOAuthResponse(
        @SerialName("access_token") val accessToken: String,
        @SerialName("expires_in") val expiresIn: Long
    )

    @Serializable
    private data class YandexUserProfile(
        val login: String,
        @SerialName("first_name") val firstName: String? = null,
        @SerialName("last_name") val lastName: String? = null,
    )
}