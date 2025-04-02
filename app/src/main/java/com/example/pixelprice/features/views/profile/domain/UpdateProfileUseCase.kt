package com.example.pixelprice.features.views.profile.domain

import com.example.pixelprice.features.views.profile.data.model.UpdateProfileRequest
import com.example.pixelprice.features.views.profile.data.repository.ProfileRepository

class UpdateProfileUseCase {
    private val repository = ProfileRepository()
    suspend operator fun invoke(id: Int, request: UpdateProfileRequest) = repository.updateProfile(id, request)
}