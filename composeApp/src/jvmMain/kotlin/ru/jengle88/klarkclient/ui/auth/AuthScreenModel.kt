package ru.jengle88.klarkclient.ui.auth

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.jengle88.klarkclient.data.network.auth.AuthManager
import ru.jengle88.klarkclient.data.network.auth.AuthProvider
import ru.jengle88.klarkclient.data.network.auth.AuthStore
import ru.jengle88.klarkclient.data.network.auth.UserProfile

class AuthScreenModel(
    private val authManager: AuthManager,
    private val authProvider: AuthProvider,
    private val authStore: AuthStore,
) : ScreenModel {
    private val _state = MutableStateFlow(
        AuthState(
            isAuthorized = authStore.accessToken.value != null,
            userNameInitials = authStore.userProfile.value?.let { getUserNameInitials(it) },
        ),
    )
    val state: StateFlow<AuthState> = _state.asStateFlow()

    private val _effects = MutableSharedFlow<AuthEffect>(extraBufferCapacity = 1)
    val effects: SharedFlow<AuthEffect> = _effects.asSharedFlow()

    fun onIntent(intent: AuthIntent) {
        when (intent) {
            is AuthIntent.Login -> handleLogin()
            is AuthIntent.Logout -> handleLogout()
        }
    }

    private fun handleLogin() {
        screenModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                val tokens = authManager.login(authProvider)
                val userProfile = authManager.getUserProfile(tokens.accessToken, authProvider)
                authStore.saveAuth(tokens.accessToken, userProfile)
                _state.update {
                    it.copy(
                        isAuthorized = true,
                        userNameInitials = getUserNameInitials(userProfile),
                        isLoading = false,
                        error = null,
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Unknown error",
                    )
                }
                _effects.tryEmit(AuthEffect.Error(e.message ?: "Unknown error"))
            }
        }
    }

    private fun handleLogout() {
        authStore.clearAuth()
        _state.update { AuthState() }
    }

    private fun getUserNameInitials(profile: UserProfile): String =
        "${profile.firstName.first()}${profile.lastName.first()}".uppercase()
}
