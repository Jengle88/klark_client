package ru.jengle88.klarkclient.data.network.auth

import kotlinx.serialization.Serializable

@Serializable
data class UserProfile(
    val firstName: String,
    val lastName: String,
)
