package ru.jengle88.klarkclient.data.network.auth

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import ru.jengle88.klarkclient.data.network.dto.UserProfile
import ru.jengle88.klarkclient.domain.api.auth.AuthStore
import java.util.prefs.Preferences

class AuthStoreImpl : AuthStore {
    private val preferences = Preferences.userNodeForPackage(AuthStoreImpl::class.java)

    private val _accessToken = MutableStateFlow<String?>(preferences.get(KEY_ACCESS_TOKEN, null))
    override val accessToken: StateFlow<String?> = _accessToken.asStateFlow()

    private val _refreshToken = MutableStateFlow<String?>(preferences.get(KEY_REFRESH_TOKEN, null))
    override val refreshToken: StateFlow<String?> = _refreshToken.asStateFlow()

    private val _userProfile = MutableStateFlow(loadUserProfile())
    override val userProfile: StateFlow<UserProfile?> = _userProfile.asStateFlow()

    override fun saveAuth(
        token: String,
        refreshToken: String?,
        userProfile: UserProfile?,
    ) {
        _accessToken.value = token
        _refreshToken.value = refreshToken
        _userProfile.value = userProfile

        preferences.put(KEY_ACCESS_TOKEN, token)
        if (refreshToken != null) {
            preferences.put(KEY_REFRESH_TOKEN, refreshToken)
        } else {
            preferences.remove(KEY_REFRESH_TOKEN)
        }
        if (userProfile != null) {
            try {
                val profileJson = Json.encodeToString(userProfile)
                preferences.put(KEY_USER_PROFILE, profileJson)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            preferences.remove(KEY_USER_PROFILE)
        }
    }

    override fun clearAuth() {
        _accessToken.value = null
        _refreshToken.value = null
        _userProfile.value = null

        preferences.remove(KEY_ACCESS_TOKEN)
        preferences.remove(KEY_REFRESH_TOKEN)
        preferences.remove(KEY_USER_PROFILE)
    }

    private fun loadUserProfile(): UserProfile? {
        val jsonString = preferences.get(KEY_USER_PROFILE, null) ?: return null
        return try {
            Json.decodeFromString(jsonString)
        } catch (e: Exception) {
            null
        }
    }

    private companion object {
        private const val KEY_ACCESS_TOKEN = "auth_access_token"
        private const val KEY_REFRESH_TOKEN = "auth_refresh_token"
        private const val KEY_USER_PROFILE = "auth_user_profile"
    }
}
