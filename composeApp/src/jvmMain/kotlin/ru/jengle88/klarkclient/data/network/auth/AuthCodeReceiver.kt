package ru.jengle88.klarkclient.data.network.auth

import io.ktor.server.cio.CIO
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationStarted
import io.ktor.server.application.call
import io.ktor.server.engine.embeddedServer
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.withTimeoutOrNull

private const val AUTH_TIMEOUT_MS = 120_000L

interface AuthCodeReceiver {
    suspend fun awaitAuthCode(port: Int, expectedState: String, onServerReady: () -> Unit): String?
}

class KtorAuthCodeReceiver : AuthCodeReceiver {
    override suspend fun awaitAuthCode(port: Int, expectedState: String, onServerReady: () -> Unit): String? {
        val codeDeferred = CompletableDeferred<String>()
        val server = embeddedServer(CIO, port) {
            routing {
                get("/") {
                    val code = call.parameters["code"]
                    val state = call.parameters["state"]

                    if (state != expectedState) {
                        call.respondText("Invalid state", status = HttpStatusCode.BadRequest)
                        codeDeferred.completeExceptionally(Exception("Invalid state: $state"))
                    } else if (code == null) {
                        val error = call.parameters["error"] ?: "Unknown error"
                        call.respondText("Error: $error", status = HttpStatusCode.BadRequest)
                        codeDeferred.completeExceptionally(Exception("Auth failed: $error"))
                    } else {
                        call.respondText("<h1>Auth Successful!</h1>", ContentType.Text.Html)
                        codeDeferred.complete(code)
                    }
                }
            }
        }
        server.environment.monitor.subscribe(ApplicationStarted) {
            onServerReady()
        }
        server.start(wait = false)

        return try {
            val code = withTimeoutOrNull(AUTH_TIMEOUT_MS) {
                codeDeferred.await()
            }
            code
        } finally {
            server.stop(gracePeriodMillis = 1000, timeoutMillis = 2000)
        }
    }
}
