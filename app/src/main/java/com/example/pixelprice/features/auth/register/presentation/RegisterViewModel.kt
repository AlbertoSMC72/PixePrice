package com.example.pixelprice.features.auth.register.presentation

import android.util.Log
import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pixelprice.core.data.FirebaseTokenProvider
import com.example.pixelprice.features.auth.register.data.model.CreateUserRequest
import com.example.pixelprice.features.auth.register.data.repository.RegisterException
import com.example.pixelprice.features.auth.register.domain.CreateUserUseCase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class RegisterUiState(
    val username: String = "",
    val email: String = "",
    val password: String = "",
    val name: String = "",
    val lastName: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val registrationSuccess: Boolean = false
)

sealed class RegisterNavigationEvent {
    object NavigateToLogin : RegisterNavigationEvent()
}

class RegisterViewModel() : ViewModel() {

    private val createUserUseCase = CreateUserUseCase()

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    private val _navigationEvent = MutableSharedFlow<RegisterNavigationEvent>()
    val navigationEvent: SharedFlow<RegisterNavigationEvent> = _navigationEvent.asSharedFlow()

    private val _toastEvent = MutableSharedFlow<String>()
    val toastEvent: SharedFlow<String> = _toastEvent.asSharedFlow()

    fun onChangeUsername(value: String) {
        _uiState.update { it.copy(username = value.trim(), errorMessage = null) }
    }
    fun onChangeEmail(value: String) {
        _uiState.update { it.copy(email = value.trim(), errorMessage = null) }
    }
    fun onChangePassword(value: String) {
        _uiState.update { it.copy(password = value, errorMessage = null) }
    }
    fun onChangeName(value: String) {
        _uiState.update { it.copy(name = value, errorMessage = null) }
    }
    fun onChangeLastName(value: String) {
        _uiState.update { it.copy(lastName = value, errorMessage = null) }
    }


    fun navigateToLogin() {
        viewModelScope.launch { _navigationEvent.emit(RegisterNavigationEvent.NavigateToLogin) }
    }

    fun onRegisterClick() {
        val currentState = _uiState.value

        if (!validateInputs(currentState)) {
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null, registrationSuccess = false) }

        val fcmToken = FirebaseTokenProvider.firebaseToken

        val userRequest = CreateUserRequest(
            email = currentState.email.trim(),
            password = currentState.password,
            firebaseToken = fcmToken,
            name = currentState.name.trim().ifEmpty { null },
            last_name = currentState.lastName.trim().ifEmpty { null }
        )

        Log.d("RegisterViewModel", "Iniciando registro para: ${userRequest.email}...")

        viewModelScope.launch {
            val result = createUserUseCase(userRequest)

            result.onSuccess { createdUser ->
                Log.i("RegisterViewModel", "Usuario registrado exitosamente en API. ID: ${createdUser.id}")
                _uiState.update { it.copy(isLoading = false, registrationSuccess = true) }
                _toastEvent.emit("¡Registro exitoso! Inicia sesión.")
                _navigationEvent.emit(RegisterNavigationEvent.NavigateToLogin)

            }.onFailure { exception ->
                handleRegistrationError(exception)
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun validateInputs(state: RegisterUiState): Boolean {
        val email = state.email.trim()
        val password = state.password

        if ( email.isBlank() || password.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Correo y contraseña son obligatorios.") }
            return false
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _uiState.update { it.copy(errorMessage = "Formato de correo inválido.") }
            return false
        }
        if (password.length < 6) {
            _uiState.update { it.copy(errorMessage = "La contraseña debe tener al menos 6 caracteres.") }
            return false
        }

        _uiState.update { it.copy(errorMessage = null) }
        return true
    }

    private fun handleRegistrationError(exception: Throwable) {
        Log.w("RegisterViewModel", "Registro fallido", exception)
        val errorMessage = when (exception) {
            is RegisterException.Conflict -> "El correo electrónico ya está en uso."
            is RegisterException.BadRequest -> "Datos inválidos. Verifica la información."
            is RegisterException.NetworkError -> "Error de red. Inténtalo de nuevo."
            is RegisterException.UnknownError -> "Error inesperado al registrar."
            else -> exception.message ?: "Error desconocido durante el registro."
        }
        _uiState.update { it.copy(errorMessage = errorMessage) }
    }
}