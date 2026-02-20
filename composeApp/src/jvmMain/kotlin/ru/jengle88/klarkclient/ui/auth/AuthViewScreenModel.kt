package ru.jengle88.klarkclient.ui.auth

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ru.jengle88.klarkclient.domain.usecase.auth.GetAuthUserStateUseCase
import ru.jengle88.klarkclient.domain.usecase.auth.LoginUseCase
import ru.jengle88.klarkclient.domain.usecase.auth.LogoutUseCase

class AuthViewScreenModel(
    private val loginUseCase: LoginUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val getAuthInfoUseCase: GetAuthUserStateUseCase,
) : ScreenModel {
    private val _state = MutableStateFlow(
        getAuthInfoUseCase.invoke().toViewState(),
    )
    val state: StateFlow<AuthViewState> = _state.asStateFlow()

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
                loginUseCase.invoke()
                _state.update {
                    getAuthInfoUseCase.invoke().toViewState(
                        isLoading = false,
                        error = null
                    )

                }
            } catch (e: Exception) {
                val errorMessage = "Authentication with failed: ${e.message ?: "Unknown error"}"
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = errorMessage,
                    )
                }
                _effects.tryEmit(AuthEffect.Error(errorMessage))
            }
        }
    }

    private fun handleLogout() {
        logoutUseCase.invoke()
        _state.update { AuthViewState() }
    }
}
