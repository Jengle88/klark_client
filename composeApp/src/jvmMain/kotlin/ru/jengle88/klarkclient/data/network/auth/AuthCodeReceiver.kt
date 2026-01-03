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

interface AuthCodeReceiver {
    suspend fun awaitAuthCode(port: Int, onServerReady: () -> Unit): String?
}

class KtorAuthCodeReceiver : AuthCodeReceiver {
    override suspend fun awaitAuthCode(port: Int, onServerReady: () -> Unit): String? {
        val codeDeferred = CompletableDeferred<String>()
        val server = embeddedServer(CIO, port) {
            routing {
                get("/") {
                    val code = call.parameters["code"]
                    if (code != null) {
                        call.respondText("<h1>Auth Successful!</h1>", ContentType.Text.Html)
                        codeDeferred.complete(code)
                    } else {
                        val error = call.parameters["error"] ?: "Unknown error"
                        call.respondText("Error: $error", status = HttpStatusCode.BadRequest)
                        codeDeferred.completeExceptionally(Exception("Auth failed: $error"))
                    }
                }
            }
        }
        server.environment.monitor.subscribe(ApplicationStarted) {
            onServerReady()
        }
        server.start(wait = false)

        return try {
            val code = codeDeferred.await()
            code
        } finally {
            server.stop(gracePeriodMillis = 1000, timeoutMillis = 2000)
        }
    }
}