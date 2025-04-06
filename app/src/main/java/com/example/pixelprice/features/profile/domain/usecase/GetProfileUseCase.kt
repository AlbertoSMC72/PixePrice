package com.example.pixelprice.features.profile.domain.usecase

import com.example.pixelprice.features.profile.data.model.GetProfileResponse
import com.example.pixelprice.features.profile.data.repository.ProfileRepository

class GetProfileUseCase {
    private val repository = ProfileRepository()

    // *** CAMBIO: Tipo de retorno ***
    suspend operator fun invoke(id: Int): Result<GetProfileResponse> = repository.getProfile(id)
}