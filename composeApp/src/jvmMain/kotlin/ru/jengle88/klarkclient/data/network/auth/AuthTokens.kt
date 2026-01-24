package ru.jengle88.klarkclient.data.network.auth

data class AuthTokens(
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Long? = null,
)