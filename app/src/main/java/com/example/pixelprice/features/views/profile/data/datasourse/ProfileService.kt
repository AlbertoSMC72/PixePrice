package com.example.pixelprice.features.views.profile.data.datasourse

import com.example.pixelprice.features.views.profile.data.model.ProfileDTO
import com.example.pixelprice.features.views.profile.data.model.UpdateProfileRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path

interface ProfileService {

    @GET("users/{id}")
    suspend fun getProfile(@Path("id") id: Int): Response<ProfileDTO>

    @PUT("users/{id}")
    suspend fun updateProfile(
        @Path("id") id: Int,
        @Body request: UpdateProfileRequest
    ): Response<ProfileDTO>
}
