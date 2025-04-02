package com.example.pixelprice.features.views.profile.precentation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pixelprice.features.views.profile.data.model.ProfileDTO
import com.example.pixelprice.features.views.profile.data.model.UpdateProfileRequest
import com.example.pixelprice.features.views.profile.domain.GetProfileUseCase
import com.example.pixelprice.features.views.profile.domain.UpdateProfileUseCase
import kotlinx.coroutines.launch

class ProfileViewModel : ViewModel() {
    private val getProfileUseCase = GetProfileUseCase()
    private val updateProfileUseCase = UpdateProfileUseCase()

    private val _profile = MutableLiveData<ProfileDTO?>()
    val profile: LiveData<ProfileDTO?> = _profile

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    fun loadProfile(userId: Int) {
        viewModelScope.launch {
            val result = getProfileUseCase(userId)
            result.onSuccess {
                _profile.value = it
            }.onFailure {
                _error.value = it.message ?: "Error al cargar el perfil"
            }
        }
    }

    fun updateProfile(userId: Int, request: UpdateProfileRequest) {
        viewModelScope.launch {
            val result = updateProfileUseCase(userId, request)
            result.onSuccess {
                _profile.value = it
            }.onFailure {
                _error.value = it.message ?: "Error al actualizar el perfil"
            }
        }
    }
}
