package com.tecsup.eventplanner_flores.ui.screens.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tecsup.eventplanner_flores.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false
)

class LoginViewModel : ViewModel() {
    private val authRepository = AuthRepository()

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onEmailChange(email: String) {
        _uiState.value = _uiState.value.copy(email = email, error = null)
    }

    fun onPasswordChange(password: String) {
        _uiState.value = _uiState.value.copy(password = password, error = null)
    }

    fun login() {
        val email = _uiState.value.email.trim()
        val password = _uiState.value.password

        if (email.isEmpty()) {
            _uiState.value = _uiState.value.copy(error = "El email es obligatorio")
            return
        }

        if (password.isEmpty()) {
            _uiState.value = _uiState.value.copy(error = "La contraseña es obligatoria")
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _uiState.value = _uiState.value.copy(error = "Email inválido")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            authRepository.login(email, password)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isSuccess = true
                    )
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = getErrorMessage(exception)
                    )
                }
        }
    }

    private fun getErrorMessage(exception: Throwable): String {
        return when {
            exception.message?.contains("password is invalid") == true ->
                "Contraseña incorrecta"
            exception.message?.contains("no user record") == true ->
                "No existe una cuenta con este email"
            exception.message?.contains("network") == true ->
                "Error de conexión. Verifica tu internet"
            else -> "Error al iniciar sesión: ${exception.message}"
        }
    }
}