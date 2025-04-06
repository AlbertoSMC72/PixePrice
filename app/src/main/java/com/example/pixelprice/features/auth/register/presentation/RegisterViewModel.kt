package com.example.pixelprice.features.auth.register.presentation

// import android.app.Application // Ya no es necesario
import android.util.Log
import android.util.Patterns
import androidx.lifecycle.ViewModel // *** CORREGIDO: Cambiar a ViewModel normal ***
import androidx.lifecycle.viewModelScope
import com.example.pixelprice.core.data.FirebaseTokenProvider
import com.example.pixelprice.features.auth.register.data.model.CreateUserRequest
import com.example.pixelprice.features.auth.register.data.repository.RegisterException
// import com.example.pixelprice.features.auth.register.data.repository.RegisterRepository // *** CORREGIDO: Ya no se usa directamente ***
import com.example.pixelprice.features.auth.register.domain.CreateUserUseCase // *** CORREGIDO: Importar UseCase ***
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

// --- Estado de la UI (sin cambios) ---
data class RegisterUiState(
    val username: String = "", // Considerar si este campo es necesario si la API no lo usa explícitamente
    val email: String = "",
    val password: String = "",
    val name: String = "", // Añadir campos si se quieren pedir en UI
    val lastName: String = "", // Añadir campos si se quieren pedir en UI
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val registrationSuccess: Boolean = false // Podría eliminarse si solo se navega
)

// --- Eventos de Navegación (sin cambios) ---
sealed class RegisterNavigationEvent {
    object NavigateToLogin : RegisterNavigationEvent()
}

// *** CORREGIDO: Cambiar a ViewModel normal ***
class RegisterViewModel() : ViewModel() { // Quitar parámetro application

    // *** CORREGIDO: Instanciar UseCase ***
    private val createUserUseCase = CreateUserUseCase()

    // --- StateFlow y SharedFlow (sin cambios) ---
    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    private val _navigationEvent = MutableSharedFlow<RegisterNavigationEvent>()
    val navigationEvent: SharedFlow<RegisterNavigationEvent> = _navigationEvent.asSharedFlow()

    private val _toastEvent = MutableSharedFlow<String>()
    val toastEvent: SharedFlow<String> = _toastEvent.asSharedFlow()

    // --- Handlers para cambios de input (Añadir name/lastName si se piden en UI) ---
    fun onChangeUsername(value: String) {
        _uiState.update { it.copy(username = value.trim(), errorMessage = null) }
    }
    fun onChangeEmail(value: String) {
        _uiState.update { it.copy(email = value.trim(), errorMessage = null) }
    }
    fun onChangePassword(value: String) {
        _uiState.update { it.copy(password = value, errorMessage = null) }
    }
    fun onChangeName(value: String) { // Si se añade a la UI
        _uiState.update { it.copy(name = value, errorMessage = null) }
    }
    fun onChangeLastName(value: String) { // Si se añade a la UI
        _uiState.update { it.copy(lastName = value, errorMessage = null) }
    }


    // --- Navegación a Login (sin cambios) ---
    fun navigateToLogin() {
        viewModelScope.launch { _navigationEvent.emit(RegisterNavigationEvent.NavigateToLogin) }
    }

    // --- Lógica de Registro ---
    fun onRegisterClick() {
        val currentState = _uiState.value

        // Validar Inputs (usando los campos del estado actual)
        if (!validateInputs(currentState)) {
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null, registrationSuccess = false) }

        val fcmToken = FirebaseTokenProvider.firebaseToken

        // Crear Request usando los datos del estado
        val userRequest = CreateUserRequest(
            // username = currentState.username, // La API no parece tener username
            email = currentState.email.trim(),
            password = currentState.password, // La contraseña no se trimea
            firebaseToken = fcmToken, // Enviar token FCM si existe
            // Usar los campos name/lastName del estado, enviar null si están vacíos
            name = currentState.name.trim().ifEmpty { null },
            last_name = currentState.lastName.trim().ifEmpty { null }
        )

        Log.d("RegisterViewModel", "Iniciando registro para: ${userRequest.email}...")

        viewModelScope.launch {
            // *** CORREGIDO: Llamar al UseCase ***
            val result = createUserUseCase(userRequest)

            result.onSuccess { createdUser ->
                Log.i("RegisterViewModel", "Usuario registrado exitosamente en API. ID: ${createdUser.id}")
                _uiState.update { it.copy(isLoading = false, registrationSuccess = true) } // Opcional: registrationSuccess
                _toastEvent.emit("¡Registro exitoso! Inicia sesión.")
                _navigationEvent.emit(RegisterNavigationEvent.NavigateToLogin) // Navegar a Login

            }.onFailure { exception ->
                handleRegistrationError(exception) // Manejar error
                _uiState.update { it.copy(isLoading = false) } // Quitar estado de carga
            }
        }
    }

    // --- Validación de Inputs (Ajustar según campos requeridos) ---
    private fun validateInputs(state: RegisterUiState): Boolean {
        // val username = state.username // Ya no se usa username si API no lo tiene
        val email = state.email.trim()
        val password = state.password
        // val name = state.name.trim() // Opcional: Validar si nombre es requerido
        // val lastName = state.lastName.trim() // Opcional: Validar si apellido es requerido

        // Quitar validación de username si no es requerido
        if (/*username.isBlank() ||*/ email.isBlank() || password.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Correo y contraseña son obligatorios.") } // Ajustar mensaje
            return false
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _uiState.update { it.copy(errorMessage = "Formato de correo inválido.") }
            return false
        }
        if (password.length < 6) { // Mantener validación de contraseña
            _uiState.update { it.copy(errorMessage = "La contraseña debe tener al menos 6 caracteres.") }
            return false
        }
        // Añadir validaciones para name/lastName si son obligatorios en tu UI/API

        _uiState.update { it.copy(errorMessage = null) } // Limpiar error si todo OK
        return true
    }

    // --- Manejo de Errores (sin cambios, pero verifica que RegisterException esté bien definido) ---
    private fun handleRegistrationError(exception: Throwable) {
        Log.w("RegisterViewModel", "Registro fallido", exception)
        val errorMessage = when (exception) {
            is RegisterException.Conflict -> "El correo electrónico ya está en uso." // Mensaje más específico
            is RegisterException.BadRequest -> "Datos inválidos. Verifica la información."
            is RegisterException.NetworkError -> "Error de red. Inténtalo de nuevo."
            is RegisterException.UnknownError -> "Error inesperado al registrar."
            // Añadir manejo de ApiException si es relevante
//            is ApiException.InvalidResponse -> "Respuesta inválida del servidor."
//            is ApiException.ServerError -> "Error del servidor (${exception.code})."
            else -> exception.message ?: "Error desconocido durante el registro."
        }
        _uiState.update { it.copy(errorMessage = errorMessage) }
    }
}