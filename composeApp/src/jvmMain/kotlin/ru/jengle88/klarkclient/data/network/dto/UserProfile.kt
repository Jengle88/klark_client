package ru.jengle88.klarkclient.data.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class UserProfile(val firstName: String, val lastName: String)
