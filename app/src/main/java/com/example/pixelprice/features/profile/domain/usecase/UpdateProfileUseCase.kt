package com.example.pixelprice.features.profile.domain.usecase

import com.example.pixelprice.features.profile.data.model.ProfileDTO
import com.example.pixelprice.features.profile.data.model.UpdateProfileRequest
import com.example.pixelprice.features.profile.data.repository.ProfileRepository

class UpdateProfileUseCase {
    private val repository = ProfileRepository()

    // Acepta los datos editables y el ID
    suspend operator fun invoke(id: Int, name: String?, lastName: String?): Result<ProfileDTO> {
        // Podrías añadir validación aquí si es compleja, pero la básica puede estar en ViewModel
        val request = UpdateProfileRequest(
            name = name?.trim()?.ifEmpty { null }, // Enviar null si está vacío después de trim
            lastName = lastName?.trim()?.ifEmpty { null }
        )
        return repository.updateProfile(id, request)
    }
}