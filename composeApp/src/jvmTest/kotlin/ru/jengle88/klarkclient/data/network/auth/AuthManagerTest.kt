package ru.jengle88.klarkclient.data.network.auth

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import ru.jengle88.klarkclient.common.UrlLauncher
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

private const val DEFAULT_PORT = 2538

class AuthManagerTest {

    private val testDispatcher = StandardTestDispatcher()

    private fun createMockHttpClient(): HttpClient {
        val mockEngine = MockEngine {
            respond(
                "{}",
                HttpStatusCode.OK,
                headers = headersOf("Content-Type" to listOf(ContentType.Application.Json.toString()))
            )
        }
        return HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }
    }

    @Test
    fun `login returns AuthTokens on successful authentication`() = runTest(testDispatcher) {
        val mockHttpClient = createMockHttpClient()
        val authHttpClient = mock<AuthHttpClient> {
            on { httpClient }.thenReturn(mockHttpClient)
        }
        val urlLauncher = mock<UrlLauncher>()
        val authCodeReceiver = mock<AuthCodeReceiver>()
        val authProvider = mock<AuthProvider>()

        val expectedTokens = AuthTokens(
            accessToken = "testAccessToken",
            refreshToken = "testRefreshToken",
            expiresIn = 3600
        )

        whenever(authCodeReceiver.awaitAuthCode(eq(DEFAULT_PORT), any(), any())).thenAnswer { invocation ->
            val onServerReady = invocation.getArgument<() -> Unit>(2)
            onServerReady()
            "testAuthCode"
        }
        whenever(authProvider.getAuthorizeUrl(any(), any())).thenReturn("https://test.auth.url")
        whenever(authProvider.exchangeCodeForToken(any(), eq("testAuthCode"), any()))
            .thenReturn(expectedTokens)

        val authManager = AuthManager(
            authHttpClient = authHttpClient,
            uriLauncher = urlLauncher,
            authCodeReceiver = authCodeReceiver,
            ioDispatcher = testDispatcher
        )

        val result = authManager.login(authProvider)

        assertEquals(expectedTokens.accessToken, result.accessToken)
        assertEquals(expectedTokens.refreshToken, result.refreshToken)
        assertEquals(expectedTokens.expiresIn, result.expiresIn)
        verify(urlLauncher).open("https://test.auth.url")
    }

    @Test
    fun `login throws IllegalStateException when auth code is null`() = runTest(testDispatcher) {
        val mockHttpClient = createMockHttpClient()
        val authHttpClient = mock<AuthHttpClient> {
            on { httpClient }.thenReturn(mockHttpClient)
        }
        val urlLauncher = mock<UrlLauncher>()
        val authCodeReceiver = mock<AuthCodeReceiver>()
        val authProvider = mock<AuthProvider>()

        whenever(authCodeReceiver.awaitAuthCode(eq(DEFAULT_PORT), any(), any())).thenAnswer { invocation ->
            val onServerReady = invocation.getArgument<() -> Unit>(2)
            onServerReady()
            null
        }
        whenever(authProvider.getAuthorizeUrl(any(), any())).thenReturn("https://test.auth.url")

        val authManager = AuthManager(
            authHttpClient = authHttpClient,
            uriLauncher = urlLauncher,
            authCodeReceiver = authCodeReceiver,
            ioDispatcher = testDispatcher
        )

        assertFailsWith<IllegalStateException> {
            authManager.login(authProvider)
        }
    }

    @Test
    fun `login opens browser with correct authorization URL`() = runTest(testDispatcher) {
        val mockHttpClient = createMockHttpClient()
        val authHttpClient = mock<AuthHttpClient> {
            on { httpClient }.thenReturn(mockHttpClient)
        }
        val urlLauncher = mock<UrlLauncher>()
        val authCodeReceiver = mock<AuthCodeReceiver>()
        val authProvider = mock<AuthProvider>()

        val expectedTokens = AuthTokens(accessToken = "token", refreshToken = null, expiresIn = 3600)

        whenever(authCodeReceiver.awaitAuthCode(any(), any(), any())).thenAnswer { invocation ->
            val onServerReady = invocation.getArgument<() -> Unit>(2)
            onServerReady()
            "code"
        }
        whenever(authProvider.getAuthorizeUrl(eq("http://localhost:$DEFAULT_PORT"), any()))
            .thenReturn("https://oauth.yandex.ru/authorize?test=true")
        whenever(authProvider.exchangeCodeForToken(any(), any(), any())).thenReturn(expectedTokens)

        val authManager = AuthManager(
            authHttpClient = authHttpClient,
            uriLauncher = urlLauncher,
            authCodeReceiver = authCodeReceiver,
            ioDispatcher = testDispatcher
        )

        authManager.login(authProvider)

        verify(authProvider).getAuthorizeUrl(eq("http://localhost:$DEFAULT_PORT"), any())
        verify(urlLauncher).open("https://oauth.yandex.ru/authorize?test=true")
    }

    @Test
    fun `getUserProfile returns UserProfile from provider`() = runTest(testDispatcher) {
        val mockHttpClient = createMockHttpClient()
        val authHttpClient = mock<AuthHttpClient> {
            on { httpClient }.thenReturn(mockHttpClient)
        }
        val urlLauncher = mock<UrlLauncher>()
        val authCodeReceiver = mock<AuthCodeReceiver>()
        val authProvider = mock<AuthProvider>()

        val expectedProfile = UserProfile(firstName = "John", lastName = "Doe")
        whenever(authProvider.getUserProfile(any(), eq("testToken"))).thenReturn(expectedProfile)

        val authManager = AuthManager(
            authHttpClient = authHttpClient,
            uriLauncher = urlLauncher,
            authCodeReceiver = authCodeReceiver,
            ioDispatcher = testDispatcher
        )

        val result = authManager.getUserProfile("testToken", authProvider)

        assertEquals("John", result.firstName)
        assertEquals("Doe", result.lastName)
        verify(authProvider).getUserProfile(mockHttpClient, "testToken")
    }

    @Test
    fun `login uses custom port when provided`() = runTest(testDispatcher) {
        val mockHttpClient = createMockHttpClient()
        val authHttpClient = mock<AuthHttpClient> {
            on { httpClient }.thenReturn(mockHttpClient)
        }
        val urlLauncher = mock<UrlLauncher>()
        val authCodeReceiver = mock<AuthCodeReceiver>()
        val authProvider = mock<AuthProvider>()

        val customPort = 8080
        val expectedTokens = AuthTokens(accessToken = "token", refreshToken = null, expiresIn = 3600)

        whenever(authCodeReceiver.awaitAuthCode(eq(customPort), any(), any())).thenAnswer { invocation ->
            val onServerReady = invocation.getArgument<() -> Unit>(2)
            onServerReady()
            "code"
        }
        whenever(authProvider.getAuthorizeUrl(eq("http://localhost:$customPort"), any()))
            .thenReturn("https://auth.url")
        whenever(authProvider.exchangeCodeForToken(any(), any(), any())).thenReturn(expectedTokens)

        val authManager = AuthManager(
            authHttpClient = authHttpClient,
            uriLauncher = urlLauncher,
            authCodeReceiver = authCodeReceiver,
            port = customPort,
            ioDispatcher = testDispatcher
        )

        authManager.login(authProvider)

        verify(authCodeReceiver).awaitAuthCode(eq(customPort), any(), any())
        verify(authProvider).getAuthorizeUrl(eq("http://localhost:$customPort"), any())
    }
}