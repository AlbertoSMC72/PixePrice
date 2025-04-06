package com.example.pixelprice.features.profile.data.repository

import android.util.Log
import com.example.pixelprice.core.network.RetrofitHelper
import com.example.pixelprice.features.profile.data.datasource.ProfileService
import com.example.pixelprice.features.profile.data.model.ProfileDTO
import com.example.pixelprice.features.profile.data.model.UpdateProfileRequest
import java.io.IOException

// Excepciones específicas para Profile (opcional pero útil)
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

    // Obtener perfil
    suspend fun getProfile(id: Int): Result<ProfileDTO> {
        if (id <= 0) return Result.failure(IllegalArgumentException("ID de usuario inválido."))
        return try {
            val response = profileService.getProfile(id)
            if (response.isSuccessful) {
                response.body()?.let { Result.success(it) }
                    ?: Result.failure(ProfileException.LoadFailed("Respuesta de perfil vacía.", response.code()))
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Error desconocido"
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

    // Actualizar perfil
    suspend fun updateProfile(id: Int, request: UpdateProfileRequest): Result<ProfileDTO> {
        if (id <= 0) return Result.failure(IllegalArgumentException("ID de usuario inválido."))
        return try {
            val response = profileService.updateProfile(id, request)
            if (response.isSuccessful) {
                response.body()?.let { Result.success(it) }
                    ?: Result.failure(ProfileException.UpdateFailed("Respuesta de actualización vacía.", response.code()))
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Error desconocido"
                if (response.code() == 404) Result.failure(ProfileException.NotFound()) // Si el usuario no existe
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