package com.tecsup.eventplanner_flores.ui.screens.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tecsup.eventplanner_flores.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class RegisterUiState(
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false
)

class RegisterViewModel : ViewModel() {
    private val authRepository = AuthRepository()

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun onEmailChange(email: String) {
        _uiState.value = _uiState.value.copy(email = email, error = null)
    }

    fun onPasswordChange(password: String) {
        _uiState.value = _uiState.value.copy(password = password, error = null)
    }

    fun onConfirmPasswordChange(confirmPassword: String) {
        _uiState.value = _uiState.value.copy(confirmPassword = confirmPassword, error = null)
    }

    fun register() {
        val email = _uiState.value.email.trim()
        val password = _uiState.value.password
        val confirmPassword = _uiState.value.confirmPassword

        if (email.isEmpty()) {
            _uiState.value = _uiState.value.copy(error = "El email es obligatorio")
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _uiState.value = _uiState.value.copy(error = "Email inválido")
            return
        }

        if (password.isEmpty()) {
            _uiState.value = _uiState.value.copy(error = "La contraseña es obligatoria")
            return
        }

        if (password.length < 6) {
            _uiState.value = _uiState.value.copy(error = "La contraseña debe tener al menos 6 caracteres")
            return
        }

        if (confirmPassword.isEmpty()) {
            _uiState.value = _uiState.value.copy(error = "Debes confirmar la contraseña")
            return
        }

        if (password != confirmPassword) {
            _uiState.value = _uiState.value.copy(error = "Las contraseñas no coinciden")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            authRepository.register(email, password)
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
            exception.message?.contains("email address is already") == true ->
                "Este email ya está registrado"
            exception.message?.contains("network") == true ->
                "Error de conexión. Verifica tu internet"
            else -> "Error al registrar: ${exception.message}"
        }
    }
}