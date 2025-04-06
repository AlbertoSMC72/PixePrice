package com.example.pixelprice.features.profile.domain.usecase

import com.example.pixelprice.features.profile.data.model.GetProfileResponse
import com.example.pixelprice.features.profile.data.model.UpdateProfileRequest
import com.example.pixelprice.features.profile.data.repository.ProfileRepository

class UpdateProfileUseCase {
    private val repository = ProfileRepository()

    // *** CAMBIO: Tipo de retorno ***
    suspend operator fun invoke(id: Int, name: String?, lastName: String?): Result<GetProfileResponse> {
        val request = UpdateProfileRequest(
            name = name?.trim()?.ifEmpty { null },
            lastName = lastName?.trim()?.ifEmpty { null }
        )
        return repository.updateProfile(id, request)
    }
}