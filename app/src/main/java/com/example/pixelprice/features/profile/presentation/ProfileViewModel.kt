package com.example.pixelprice.features.profile.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pixelprice.core.data.TokenManager
import com.example.pixelprice.core.data.UserInfoProvider
import com.example.pixelprice.features.profile.data.model.ProfileDTO
import com.example.pixelprice.features.profile.domain.usecase.GetProfileUseCase
import com.example.pixelprice.features.profile.domain.usecase.UpdateProfileUseCase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

// Estado de la UI actualizado
data class ProfileUiState(
    val profileData: ProfileDTO? = null, // Los datos originales de la API
    val email: String = "", // Email (solo lectura)
    val name: String = "", // Campo editable para nombre
    val lastName: String = "", // Campo editable para apellido
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val errorMessage: String? = null
)

// Eventos (sin cambios)
sealed class ProfileEvent {
    data class ShowToast(val message: String) : ProfileEvent()
    object NavigateToLogin : ProfileEvent()
}

class ProfileViewModel() : ViewModel() { // No necesita Application
    private val getProfileUseCase = GetProfileUseCase()
    private val updateProfileUseCase = UpdateProfileUseCase()

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val _eventFlow = MutableSharedFlow<ProfileEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    fun loadProfile(userId: Int) {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            val result = getProfileUseCase(userId)
            result.onSuccess { profile ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        profileData = profile,
                        email = profile.email, // Cargar email original
                        name = profile.name ?: "", // Cargar nombre o vacío
                        lastName = profile.lastName ?: "" // Cargar apellido o vacío
                    )
                }
                Log.i("ProfileViewModel", "Perfil cargado para userId $userId")
            }.onFailure { exception ->
                Log.e("ProfileViewModel", "Error al cargar perfil $userId", exception)
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = exception.message ?: "Error al cargar el perfil")
                }
            }
        }
    }

    // --- Handlers para cambios en los campos editables ---
    fun onNameChange(value: String) {
        _uiState.update { it.copy(name = value, errorMessage = null) } // Limpiar error al editar
    }
    fun onLastNameChange(value: String) {
        _uiState.update { it.copy(lastName = value, errorMessage = null) }
    }
    // No hay onEmailChange si es solo lectura

    fun updateProfile(userId: Int) {
        val currentState = _uiState.value

        // Validación simple (podría ser más robusta)
        // if (currentState.name.isBlank()) {
        //      viewModelScope.launch { _eventFlow.emit(ProfileEvent.ShowToast("El nombre no puede estar vacío")) }
        //     return
        // }

        _uiState.update { it.copy(isSaving = true, errorMessage = null) }

        viewModelScope.launch {
            // Usar los valores actuales del estado de la UI (name, lastName)
            val result = updateProfileUseCase(
                id = userId,
                name = currentState.name, // Ya está en el estado
                lastName = currentState.lastName // Ya está en el estado
            )
            result.onSuccess { updatedProfile ->
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        profileData = updatedProfile, // Actualiza el DTO base
                        name = updatedProfile.name ?: "", // Refleja el cambio guardado
                        lastName = updatedProfile.lastName ?: ""
                    )
                }
                Log.i("ProfileViewModel", "Perfil actualizado para userId $userId")
                _eventFlow.emit(ProfileEvent.ShowToast("Perfil actualizado correctamente"))
            }.onFailure { exception ->
                Log.e("ProfileViewModel", "Error al actualizar perfil $userId", exception)
                _uiState.update {
                    it.copy(isSaving = false, errorMessage = exception.message ?: "Error al guardar cambios")
                }
                _eventFlow.emit(ProfileEvent.ShowToast("Error al guardar: ${exception.message}"))
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            Log.i("ProfileViewModel", "Cerrando sesión...")
            TokenManager.clearToken()
            UserInfoProvider.clearUserInfo()
            _eventFlow.emit(ProfileEvent.NavigateToLogin)
        }
    }
}

