package com.example.pixelprice.features.profile.presentation

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel // *** CAMBIO: Usar AndroidViewModel ***
import androidx.lifecycle.viewModelScope
import com.example.pixelprice.core.data.TokenManager
import com.example.pixelprice.core.data.UserInfoProvider // Se sigue usando para limpiar al logout
// *** CAMBIO: Importar GetProfileResponse ***
import com.example.pixelprice.features.profile.data.model.GetProfileResponse
import com.example.pixelprice.features.profile.domain.usecase.GetProfileUseCase
import com.example.pixelprice.features.profile.domain.usecase.UpdateProfileUseCase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

// Estado de la UI (actualizado con downloadDirectoryUri)
data class ProfileUiState(
    val profileData: GetProfileResponse? = null, // Almacena la respuesta completa
    val email: String = "",
    val name: String = "",
    val lastName: String = "",
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val downloadDirectoryUri: String? = null
)

// Eventos (actualizado con OpenDirectoryPicker)
sealed class ProfileEvent {
    data class ShowToast(val message: String) : ProfileEvent()
    object NavigateToLogin : ProfileEvent()
    object OpenDirectoryPicker : ProfileEvent()
}

// *** CAMBIO: Heredar de AndroidViewModel y recibir Application ***
class ProfileViewModel(application: Application) : AndroidViewModel(application) {
    private val getProfileUseCase = GetProfileUseCase()
    private val updateProfileUseCase = UpdateProfileUseCase()

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val _eventFlow = MutableSharedFlow<ProfileEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    // Claves y SharedPreferences para la carpeta de descarga
    companion object {
        private const val PREFS_NAME_PROFILE = "pixelprice_profile_prefs"
        private const val KEY_DOWNLOAD_DIR_URI = "download_directory_uri"
    }

    private val profilePrefs: SharedPreferences by lazy {
        getApplication<Application>().getSharedPreferences(PREFS_NAME_PROFILE, Context.MODE_PRIVATE)
    }

    init {
        loadDownloadDirectoryPreference()
        // La carga del perfil se inicia desde la UI con el userId
    }

    private fun loadDownloadDirectoryPreference() {
        val savedUriString = profilePrefs.getString(KEY_DOWNLOAD_DIR_URI, null)
        if (savedUriString != null) {
            try {
                val uri = Uri.parse(savedUriString)
                // Intenta tomar permisos persistentes
                getApplication<Application>().contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                _uiState.update { it.copy(downloadDirectoryUri = savedUriString) }
                Log.d("ProfileViewModel", "Preferencia de directorio de descarga cargada: $savedUriString")
            } catch (e: SecurityException) {
                Log.w("ProfileViewModel", "Permiso perdido para el directorio guardado: $savedUriString. Limpiando preferencia.", e)
                clearDownloadDirectoryPreference()
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Error al parsear o verificar URI guardado: $savedUriString", e)
                clearDownloadDirectoryPreference()
            }
        }
    }

    // Carga de perfil (actualizado para extraer datos de la estructura correcta)
    fun loadProfile(userId: Int) {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            val result = getProfileUseCase(userId)
            result.onSuccess { profileResponse -> // Ahora es GetProfileResponse
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        profileData = profileResponse, // Guardar la respuesta completa
                        // *** Extraer de profileResponse.data.user ***
                        email = profileResponse.data?.user?.email ?: "",
                        name = profileResponse.data?.user?.name ?: "",
                        lastName = profileResponse.data?.user?.lastName ?: ""
                    )
                }
                Log.i("ProfileViewModel", "Perfil cargado para userId $userId. Data: ${profileResponse.data?.user}")
            }.onFailure { exception ->
                Log.e("ProfileViewModel", "Error al cargar perfil $userId", exception)
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = exception.message ?: "Error al cargar el perfil")
                }
            }
        }
    }

    // Handlers de cambio (sin cambios)
    fun onNameChange(value: String) {
        _uiState.update { it.copy(name = value, errorMessage = null) }
    }
    fun onLastNameChange(value: String) {
        _uiState.update { it.copy(lastName = value, errorMessage = null) }
    }

    // Actualizar perfil (actualizado para extraer datos de la estructura correcta)
    fun updateProfile(userId: Int) {
        val currentState = _uiState.value
        _uiState.update { it.copy(isSaving = true, errorMessage = null) }

        viewModelScope.launch {
            val result = updateProfileUseCase(
                id = userId,
                name = currentState.name,
                lastName = currentState.lastName
            )
            result.onSuccess { updatedProfileResponse -> // Ahora es GetProfileResponse
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        profileData = updatedProfileResponse, // Guardar respuesta completa
                        // *** Extraer de updatedProfileResponse.data.user ***
                        name = updatedProfileResponse.data?.user?.name ?: "",
                        lastName = updatedProfileResponse.data?.user?.lastName ?: ""
                        // El email no cambia, no es necesario actualizarlo aquí
                    )
                }
                Log.i("ProfileViewModel", "Perfil actualizado para userId $userId. Data: ${updatedProfileResponse.data?.user}")
                _eventFlow.emit(ProfileEvent.ShowToast("Perfil actualizado correctamente"))
            }.onFailure { exception ->
                Log.e("ProfileViewModel", "Error al actualizar perfil $userId", exception)
                _uiState.update {
                    it.copy(isSaving = false, errorMessage = exception.message ?: "Error al guardar cambios")
                }
                // Considerar si mostrar el mismo mensaje de error en el toast
                _eventFlow.emit(ProfileEvent.ShowToast("Error al guardar: ${exception.message ?: "Error desconocido"}"))
            }
        }
    }

    // Funciones para selección de directorio (sin cambios respecto a la respuesta anterior)
    fun selectDownloadDirectory() {
        viewModelScope.launch {
            _eventFlow.emit(ProfileEvent.OpenDirectoryPicker)
        }
    }

    fun onDirectorySelected(uri: Uri?) {
        if (uri != null) {
            viewModelScope.launch {
                try {
                    val contentResolver = getApplication<Application>().contentResolver
                    val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    contentResolver.takePersistableUriPermission(uri, takeFlags)
                    val uriString = uri.toString()
                    profilePrefs.edit().putString(KEY_DOWNLOAD_DIR_URI, uriString).apply()
                    _uiState.update { it.copy(downloadDirectoryUri = uriString) }
                    _eventFlow.emit(ProfileEvent.ShowToast("Carpeta de descarga actualizada"))
                    Log.i("ProfileViewModel", "Directorio de descarga guardado: $uriString")
                } catch (e: SecurityException) {
                    Log.e("ProfileViewModel", "No se pudo obtener permiso persistente para el directorio: $uri", e)
                    _eventFlow.emit(ProfileEvent.ShowToast("Error al guardar selección de carpeta (permiso)"))
                    clearDownloadDirectoryPreference()
                } catch (e: Exception) {
                    Log.e("ProfileViewModel", "Error al procesar URI del directorio seleccionado: $uri", e)
                    _eventFlow.emit(ProfileEvent.ShowToast("Error al guardar selección de carpeta"))
                    clearDownloadDirectoryPreference()
                }
            }
        } else {
            Log.w("ProfileViewModel", "Selección de directorio cancelada o URI nulo.")
        }
    }

    private fun clearDownloadDirectoryPreference() {
        profilePrefs.edit().remove(KEY_DOWNLOAD_DIR_URI).apply()
        _uiState.update { it.copy(downloadDirectoryUri = null) }
    }


    // Logout (sin cambios)
    fun logout() {
        viewModelScope.launch {
            Log.i("ProfileViewModel", "Cerrando sesión...")
            TokenManager.clearToken()
            // UserInfoProvider.clearUserInfo() // clearToken debería encargarse si se centraliza allí
            _eventFlow.emit(ProfileEvent.NavigateToLogin)
        }
    }
}