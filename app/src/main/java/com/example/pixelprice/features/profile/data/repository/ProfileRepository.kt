package com.example.pixelprice.features.profile.data.repository

import android.util.Log
import com.example.pixelprice.core.network.RetrofitHelper
import com.example.pixelprice.features.profile.data.datasource.ProfileService
import com.example.pixelprice.features.profile.data.model.GetProfileResponse
import com.example.pixelprice.features.profile.data.model.UpdateProfileRequest
import java.io.IOException

// Excepciones específicas para Profile (sin cambios)
sealed class ProfileException(message: String, cause: Throwable? = null) : Exception(message, cause) {
    class NetworkError(cause: Throwable) : ProfileException("Error de red. Verifica tu conexión.", cause)
    class NotFound(message: String = "Usuario no encontrado.") : ProfileException(message)
    class UpdateFailed(message: String, code: Int? = null) : ProfileException("Error al actualizar ($code): $message")
    class LoadFailed(message: String, code: Int? = null) : ProfileException("Error al cargar ($code): $message")
    class UnknownError(cause: Throwable) : ProfileException("Error inesperado.", cause)
}


class ProfileRepository {
    private val profileService: ProfileService by lazy {
        RetrofitHelper.createService(ProfileService::class.java)
    }

    // Obtener perfil (actualizado para GetProfileResponse)
    suspend fun getProfile(id: Int): Result<GetProfileResponse> {
        if (id <= 0) return Result.failure(IllegalArgumentException("ID de usuario inválido."))
        return try {
            val response = profileService.getProfile(id)
            if (response.isSuccessful) {
                val profileResponse = response.body()
                // *** Validar la estructura completa ***
                if (profileResponse?.data?.user != null) {
                    Log.d("ProfileRepository", "Perfil recibido de API: ${profileResponse.data.user}")
                    Result.success(profileResponse)
                } else {
                    Log.w("ProfileRepository", "Respuesta API OK para getProfile, pero estructura inválida. Body: $profileResponse")
                    Result.failure(ProfileException.LoadFailed("Respuesta de perfil inválida o vacía.", response.code()))
                }
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Error desconocido"
                Log.w("ProfileRepository", "Error API (${response.code()}) al cargar perfil: $errorMsg")
                if (response.code() == 404) Result.failure(ProfileException.NotFound())
                else Result.failure(ProfileException.LoadFailed(errorMsg, response.code()))
            }
        } catch (e: IOException) {
            Log.e("ProfileRepository", "Error de red cargando perfil", e)
            Result.failure(ProfileException.NetworkError(e))
        } catch (e: Exception) {
            Log.e("ProfileRepository", "Error inesperado cargando perfil", e)
            Result.failure(ProfileException.UnknownError(e))
        }
    }

    // Actualizar perfil (actualizado para GetProfileResponse)
    suspend fun updateProfile(id: Int, request: UpdateProfileRequest): Result<GetProfileResponse> {
        if (id <= 0) return Result.failure(IllegalArgumentException("ID de usuario inválido."))
        return try {
            val response = profileService.updateProfile(id, request)
            if (response.isSuccessful) {
                val updatedProfileResponse = response.body()
                // *** Validar la estructura completa ***
                if (updatedProfileResponse?.data?.user != null) {
                    Log.d("ProfileRepository", "Perfil actualizado recibido de API: ${updatedProfileResponse.data.user}")
                    Result.success(updatedProfileResponse)
                } else {
                    Log.w("ProfileRepository", "Respuesta API OK para updateProfile, pero estructura inválida. Body: $updatedProfileResponse")
                    Result.failure(ProfileException.UpdateFailed("Respuesta de actualización inválida o vacía.", response.code()))
                }
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Error desconocido"
                Log.w("ProfileRepository", "Error API (${response.code()}) al actualizar perfil: $errorMsg")
                if (response.code() == 404) Result.failure(ProfileException.NotFound())
                else Result.failure(ProfileException.UpdateFailed(errorMsg, response.code()))
            }
        } catch (e: IOException) {
            Log.e("ProfileRepository", "Error de red actualizando perfil", e)
            Result.failure(ProfileException.NetworkError(e))
        } catch (e: Exception) {
            Log.e("ProfileRepository", "Error inesperado actualizando perfil", e)
            Result.failure(ProfileException.UnknownError(e))
        }
    }
}