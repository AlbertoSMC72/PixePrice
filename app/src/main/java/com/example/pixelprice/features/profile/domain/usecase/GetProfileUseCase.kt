package com.example.pixelprice.features.profile.domain.usecase

import com.example.pixelprice.features.profile.data.model.ProfileDTO
import com.example.pixelprice.features.profile.data.repository.ProfileRepository

class GetProfileUseCase {
    private val repository = ProfileRepository()

    // Retorna el DTO del perfil o falla con una excepción específica
    suspend operator fun invoke(id: Int): Result<ProfileDTO> = repository.getProfile(id)
}