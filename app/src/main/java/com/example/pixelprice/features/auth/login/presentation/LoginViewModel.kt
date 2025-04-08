package com.example.pixelprice.features.auth.login.presentation

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pixelprice.core.data.FirebaseTokenProvider
import com.example.pixelprice.features.auth.login.data.repository.AuthException
import com.example.pixelprice.features.auth.login.domain.LoginUseCase
import com.example.pixelprice.features.deviceToken.domain.RegisterDeviceTokenUseCase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

sealed class LoginNavigationEvent {
    object NavigateToProjectList : LoginNavigationEvent()
    object NavigateToRegister : LoginNavigationEvent()
}

class LoginViewModel() : ViewModel() {

    private val loginUseCase = LoginUseCase()
    private val registerDeviceTokenUseCase = RegisterDeviceTokenUseCase()

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _toastEvent = MutableSharedFlow<String>()
    val toastEvent: SharedFlow<String> = _toastEvent.asSharedFlow()

    private val _navigationEvent = MutableSharedFlow<LoginNavigationEvent>()
    val navigationEvent: SharedFlow<LoginNavigationEvent> = _navigationEvent.asSharedFlow()

    fun onEmailChange(newEmail: String) {
        _uiState.update { it.copy(email = newEmail, errorMessage = null) }
    }

    fun onPasswordChange(newPassword: String) {
        _uiState.update { it.copy(password = newPassword, errorMessage = null) }
    }

    fun onLoginClick() {
        val currentState = _uiState.value
        val email = currentState.email.trim()
        val password = currentState.password

        if (email.isBlank() || password.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Correo y contraseña son obligatorios") }
            return
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _uiState.update { it.copy(errorMessage = "Formato de correo inválido") }
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            val loginResult = loginUseCase(email, password)
            loginResult.onSuccess {
                Log.i("LoginViewModel", "Login exitoso.")
                registerFcmTokenInBackground()
                _navigationEvent.emit(LoginNavigationEvent.NavigateToProjectList)
            }.onFailure { exception ->
                handleLoginError(exception)
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }


    fun navigateToRegister() {
        viewModelScope.launch {
            _navigationEvent.emit(LoginNavigationEvent.NavigateToRegister)
        }
    }

    private fun handleLoginError(exception: Throwable) {
        Log.w("LoginViewModel", "Login fallido", exception)
        val errorMessage = when (exception) {
            is AuthException.InvalidCredentials -> "Correo o contraseña incorrectos."
            is AuthException.NetworkError -> "Error de red. Inténtalo de nuevo."
            else -> exception.message ?: "Error desconocido durante el login"
        }
        _uiState.update { it.copy(errorMessage = errorMessage) }
    }

    private fun registerFcmTokenInBackground() {
        val fcmToken = FirebaseTokenProvider.firebaseToken
        if (fcmToken != null) {
            viewModelScope.launch {
                try {
                    val registerResult = registerDeviceTokenUseCase(fcmToken)
                    registerResult.onSuccess {
                        Log.i("LoginViewModel", "Token FCM registrado en background.")
                    }.onFailure { e ->
                        Log.w("LoginViewModel", "Fallo al registrar token FCM en background.", e)
                        _toastEvent.emit("No se pudo actualizar el token de notificaciones.")
                    }
                } catch (e: Exception) {
                    Log.e("LoginViewModel", "Excepción registrando token FCM en background", e)
                    _toastEvent.emit("Error interno al registrar token.")
                }
            }
        } else {
            Log.w("LoginViewModel", "No se encontró token FCM para registrar después del login.")
            viewModelScope.launch { _toastEvent.emit("No se pudo obtener el token para notificaciones.") }
        }
    }
}