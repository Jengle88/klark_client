package ru.jengle88.klarkclient.ui.auth

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ru.jengle88.klarkclient.common.CoroutineDispatchers
import ru.jengle88.klarkclient.domain.usecase.auth.GetAuthUserStateUseCase
import ru.jengle88.klarkclient.domain.usecase.auth.LoginUseCase
import ru.jengle88.klarkclient.domain.usecase.auth.LogoutUseCase

class AuthViewScreenModel(
    private val loginUseCase: LoginUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val getAuthInfoUseCase: GetAuthUserStateUseCase,
    private val coroutineDispatchers: CoroutineDispatchers,
) : ScreenModel {
    private val _state = MutableStateFlow(AuthViewState(isLoading = true))
    val state: StateFlow<AuthViewState> = _state.asStateFlow()

    private val _effects = MutableSharedFlow<AuthEffect>(extraBufferCapacity = 1)
    val effects: SharedFlow<AuthEffect> = _effects.asSharedFlow()

    init {
        screenModelScope.launch(coroutineDispatchers.io) {
            _state.value = getAuthInfoUseCase.invoke().toViewState()
        }
    }

    fun onIntent(intent: AuthIntent) {
        when (intent) {
            is AuthIntent.Login -> handleLogin()
            is AuthIntent.Logout -> handleLogout()
        }
    }

    private fun handleLogin() {
        _state.update { it.copy(isLoading = true, error = null) }
        screenModelScope.launch(coroutineDispatchers.io) {
            try {
                loginUseCase()
                _state.update {
                    getAuthInfoUseCase().toViewState(
                        isLoading = false,
                        error = null
                    )
                }
            } catch (e: Exception) {
                val errorMessage = "Authentication with failed"
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
        screenModelScope.launch(coroutineDispatchers.io) {
            logoutUseCase()
        }
        _state.update { AuthViewState() }
    }
}
