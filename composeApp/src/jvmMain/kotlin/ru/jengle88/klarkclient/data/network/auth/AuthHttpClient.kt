package ru.jengle88.klarkclient.data.network.auth

import io.ktor.client.HttpClient

class AuthHttpClient(client: HttpClient) {
    val httpClient: HttpClient = client
}
