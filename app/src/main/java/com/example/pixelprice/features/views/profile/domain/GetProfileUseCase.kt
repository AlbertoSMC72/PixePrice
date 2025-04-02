package com.example.pixelprice.features.views.profile.domain

import com.example.pixelprice.features.views.profile.data.repository.ProfileRepository

class GetProfileUseCase {
    private val repository = ProfileRepository()
    suspend operator fun invoke(id: Int) = repository.getProfile(id)
}