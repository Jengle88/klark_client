package ru.jengle88.klarkclient.data.network.auth

data class AuthTokens(
    val accessToken: String,
    val expiresIn: Long? = null,
)