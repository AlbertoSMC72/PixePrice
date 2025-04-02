package com.example.pixelprice.features.views.profile.data.repository

import com.example.pixelprice.core.network.RetrofitHelper
import com.example.pixelprice.features.views.profile.data.datasourse.ProfileService
import com.example.pixelprice.features.views.profile.data.model.ProfileDTO
import com.example.pixelprice.features.views.profile.data.model.UpdateProfileRequest

class ProfileRepository {
    private val profileService = RetrofitHelper.createService(ProfileService::class.java)

    suspend fun getProfile(id: Int): Result<ProfileDTO> = try {
        val response = profileService.getProfile(id)
        if (response.isSuccessful) Result.success(response.body()!!)
        else Result.failure(Exception(response.errorBody()?.string()))
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun updateProfile(id: Int, request: UpdateProfileRequest): Result<ProfileDTO> = try {
        val response = profileService.updateProfile(id, request)
        if (response.isSuccessful) Result.success(response.body()!!)
        else Result.failure(Exception(response.errorBody()?.string()))
    } catch (e: Exception) {
        Result.failure(e)
    }
}
