package ru.jengle88.klarkclient.data.network.auth

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class YandexAuthProviderTest {

    private val clientId = "testClientId"
    private val clientSecret = "testClientSecret"
    private val redirectUri = "http://localhost/callback"
    private val authProvider = YandexAuthProvider(clientId, clientSecret)

    fun createHttpClient(mockEngine: MockEngine): HttpClient {
        return HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                })
            }
        }
    }

    @Test
    fun `getAuthorizeUrl returns correct URL`() {
        val state = UUID.randomUUID().toString()
        val expectedUrl =
            "https://oauth.yandex.ru/authorize?response_type=code&client_id=$clientId&redirect_uri=$redirectUri&state=$state"

        val result = authProvider.getAuthorizeUrl(redirectUri, state = state)

        assertEquals(expectedUrl, result)
    }

    @Test
    fun `exchangeCodeForToken returns valid AuthTokens on success`() = runBlocking {
        val code = "testCode"
        val mockResponse = """
            {
                "access_token": "testAccessToken",
                "expires_in": 3600
            }
        """
        val mockEngine = MockEngine { request ->
            assertEquals("https://oauth.yandex.ru/token", request.url.toString())
            assertEquals(HttpMethod.Post, request.method)
            respond(
                mockResponse,
                HttpStatusCode.OK,
                headers = headersOf("Content-Type" to listOf(ContentType.Application.Json.toString()))
            )
        }
        val httpClient = createHttpClient(mockEngine)

        val result = authProvider.exchangeCodeForToken(httpClient, code, redirectUri)

        assertEquals("testAccessToken", result.accessToken)
        assertEquals(3600, result.expiresIn)
    }

    @Test
    fun `exchangeCodeForToken throws exception on error response`() = runBlocking {
        val code = "testCode"
        val mockEngine = MockEngine {
            respond("", HttpStatusCode.BadRequest)
        }
        val httpClient = createHttpClient(mockEngine)

        assertFailsWith<Exception> {
            authProvider.exchangeCodeForToken(httpClient, code, redirectUri)
        }
        return@runBlocking
    }

    @Test
    fun `getUserProfile returns valid UserProfile on success`() = runBlocking {
        val accessToken = "testAccessToken"
        val mockResponse = """
            {
                "login": "testLogin",
                "first_name": "Test",
                "last_name": "User"
            }
        """
        val mockEngine = MockEngine { request ->
            val uri = URLBuilder("https://login.yandex.ru/info").apply {
                parameters.append("format", "json")
            }.buildString()
            assertEquals(uri, request.url.toString())
            assertEquals("OAuth $accessToken", request.headers["Authorization"])
            respond(
                mockResponse,
                HttpStatusCode.OK,
                headers = headersOf("Content-Type" to listOf(ContentType.Application.Json.toString()))
            )
        }
        val httpClient = createHttpClient(mockEngine)

        val result = authProvider.getUserProfile(httpClient, accessToken)

        assertEquals("Test", result.firstName)
        assertEquals("User", result.lastName)
    }

    @Test
    fun `getUserProfile returns empty strings for missing fields`() = runBlocking {
        val accessToken = "testAccessToken"
        val mockResponse = """
            {
                "login": "testLogin"
            }
        """
        val mockEngine = MockEngine {
            respond(
                mockResponse,
                HttpStatusCode.OK,
                headers = headersOf("Content-Type" to listOf(ContentType.Application.Json.toString()))
            )
        }
        val httpClient = createHttpClient(mockEngine)

        val result = authProvider.getUserProfile(httpClient, accessToken)

        assertEquals("", result.firstName)
        assertEquals("", result.lastName)
    }
}