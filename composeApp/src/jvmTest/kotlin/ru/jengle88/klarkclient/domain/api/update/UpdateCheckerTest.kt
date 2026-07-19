package ru.jengle88.klarkclient.domain.api.update

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondError
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpStatusCode
import io.ktor.http.fullPath
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class UpdateCheckerTest {
    private val owner = "KlarkTeam"
    private val repo = "klark-client"

    private fun createHttpClient(mockEngine: MockEngine): HttpClient =
        HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json(
                    Json {
                        ignoreUnknownKeys = true
                        isLenient = true
                    },
                )
            }
        }

    @Test
    fun `checkForUpdate returns update info when newer version is available`() =
        runBlocking {
            val mockEngine =
                MockEngine { request ->
                    assertEquals(
                        "/repos/$owner/$repo/releases/latest",
                        request.url.fullPath,
                    )
                    respond(
                        """
                        {
                            "tag_name": "v1.1.0",
                            "html_url": "https://github.com/$owner/$repo/releases/tag/v1.1.0"
                        }
                        """.trimIndent(),
                        HttpStatusCode.OK,
                        headers = headersOf("Content-Type" to listOf("application/json")),
                    )
                }
            val updateChecker =
                UpdateChecker(
                    client = createHttpClient(mockEngine),
                    owner = owner,
                    repo = repo,
                )

            val result = updateChecker.checkForUpdate("1.0.0")

            assertNotNull(result)
            assertEquals("1.1.0", result.version)
            assertEquals("https://github.com/$owner/$repo/releases/tag/v1.1.0", result.downloadUrl)
        }

    @Test
    fun `checkForUpdate returns null when current version is up to date`() =
        runBlocking {
            val mockEngine =
                MockEngine {
                    respond(
                        """
                        {
                            "tag_name": "1.0.0",
                            "html_url": "https://github.com/$owner/$repo/releases/tag/1.0.0"
                        }
                        """.trimIndent(),
                        HttpStatusCode.OK,
                        headers = headersOf("Content-Type" to listOf("application/json")),
                    )
                }
            val updateChecker =
                UpdateChecker(
                    client = createHttpClient(mockEngine),
                    owner = owner,
                    repo = repo,
                )

            val result = updateChecker.checkForUpdate("1.0.0")

            assertNull(result)
        }

    @Test
    fun `checkForUpdate returns null when current version is newer than latest`() =
        runBlocking {
            val mockEngine =
                MockEngine {
                    respond(
                        """
                        {
                            "tag_name": "1.0.0",
                            "html_url": "https://github.com/$owner/$repo/releases/tag/1.0.0"
                        }
                        """.trimIndent(),
                        HttpStatusCode.OK,
                        headers = headersOf("Content-Type" to listOf("application/json")),
                    )
                }
            val updateChecker =
                UpdateChecker(
                    client = createHttpClient(mockEngine),
                    owner = owner,
                    repo = repo,
                )

            val result = updateChecker.checkForUpdate("1.1.0")

            assertNull(result)
        }

    @Test
    fun `checkForUpdate returns null on error response`() =
        runBlocking {
            val mockEngine = MockEngine { _ -> respondError(HttpStatusCode.NotFound) }
            val updateChecker =
                UpdateChecker(
                    client = createHttpClient(mockEngine),
                    owner = owner,
                    repo = repo,
                )

            val result = updateChecker.checkForUpdate("1.0.0")

            assertNull(result)
        }

    @Test
    fun `checkForUpdate returns null on invalid json response`() =
        runBlocking {
            val mockEngine =
                MockEngine {
                    respond(
                        "not-json",
                        HttpStatusCode.OK,
                        headers = headersOf("Content-Type" to listOf("application/json")),
                    )
                }
            val updateChecker =
                UpdateChecker(
                    client = createHttpClient(mockEngine),
                    owner = owner,
                    repo = repo,
                )

            val result = updateChecker.checkForUpdate("1.0.0")

            assertNull(result)
        }

    @Test
    fun `checkForUpdate handles v prefix in tag name`() =
        runBlocking {
            val mockEngine =
                MockEngine {
                    respond(
                        """
                        {
                            "tag_name": "v2.0.0",
                            "html_url": "https://github.com/$owner/$repo/releases/tag/v2.0.0"
                        }
                        """.trimIndent(),
                        HttpStatusCode.OK,
                        headers = headersOf("Content-Type" to listOf("application/json")),
                    )
                }
            val updateChecker =
                UpdateChecker(
                    client = createHttpClient(mockEngine),
                    owner = owner,
                    repo = repo,
                )

            val result = updateChecker.checkForUpdate("1.0.0")

            assertNotNull(result)
            assertEquals("2.0.0", result.version)
        }
}
