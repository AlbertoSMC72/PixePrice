package com.example.pixelprice.features.profile.data.datasource

import com.example.pixelprice.features.profile.data.model.GetProfileResponse
import com.example.pixelprice.features.profile.data.model.UpdateProfileRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Path

interface ProfileService {
    @GET("/users/{id}")
    suspend fun getProfile(@Path("id") id: Int): Response<GetProfileResponse>

    @PATCH("/users/{id}")
    suspend fun updateProfile(
        @Path("id") id: Int,
        @Body request: UpdateProfileRequest
    ): Response<GetProfileResponse>
}