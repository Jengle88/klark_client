package ru.jengle88.klarkclient.data.network.dto

data class AuthTokens(
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Long? = null
)
