package com.example.pixelprice.features.profile.data.datasource

import com.example.pixelprice.features.profile.data.model.ProfileDTO
import com.example.pixelprice.features.profile.data.model.UpdateProfileRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Path

interface ProfileService {
    // Obtener datos del perfil del usuario
    @GET("/users/{id}") // Asume que tu endpoint es este
    suspend fun getProfile(@Path("id") id: Int): Response<ProfileDTO>

    // Actualizar datos del perfil del usuario
    @PATCH("/users/{id}") // Asume que tu endpoint es este
    suspend fun updateProfile(
        @Path("id") id: Int,
        @Body request: UpdateProfileRequest // Usa el DTO de request actualizado
    ): Response<ProfileDTO> // La API podría devolver el perfil actualizado
}