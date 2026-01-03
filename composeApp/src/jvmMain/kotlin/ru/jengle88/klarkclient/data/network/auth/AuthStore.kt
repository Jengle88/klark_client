package ru.jengle88.klarkclient.data.network.auth

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

interface AuthStore {
    val accessToken: StateFlow<String?>
    val userProfile: StateFlow<UserProfile?>
    fun saveAuth(token: String, userProfile: UserProfile?)
    fun clearAuth()
}

class AuthStoreImpl : AuthStore {
    private val _accessToken = MutableStateFlow<String?>(null)
    override val accessToken: StateFlow<String?> = _accessToken.asStateFlow()

    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    override val userProfile: StateFlow<UserProfile?> = _userProfile.asStateFlow()

    override fun saveAuth(token: String, userProfile: UserProfile?) {
        _accessToken.value = token
        _userProfile.value = userProfile
    }

    override fun clearAuth() {
        _accessToken.value = null
        _userProfile.value = null
    }
}
