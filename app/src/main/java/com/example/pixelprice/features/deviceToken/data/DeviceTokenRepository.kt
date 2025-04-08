package com.example.pixelprice.features.deviceToken.data

import android.util.Log
import com.example.pixelprice.core.network.RetrofitHelper
import com.example.pixelprice.core.service.DeviceTokenService
import com.example.pixelprice.core.service.RegisterTokenRequest
import java.io.IOException

sealed class DeviceTokenException(message: String, cause: Throwable? = null) : Exception(message, cause) {
    class NetworkError(cause: Throwable) : DeviceTokenException("Error de red al gestionar token.", cause)
    class ApiError(message: String, val code: Int?) : DeviceTokenException("Error API ($code): $message")
    class UnknownError(cause: Throwable) : DeviceTokenException("Error inesperado.", cause)
}


class DeviceTokenRepository {

    private val deviceTokenService: DeviceTokenService by lazy {
        RetrofitHelper.createService(DeviceTokenService::class.java)
    }

    suspend fun registerTokenWithApi(fcmToken: String): Result<Unit> {
        return try {
            Log.d("DeviceTokenRepository", "Intentando registrar token FCM en API: ${fcmToken.take(10)}...")
            val request = RegisterTokenRequest(token = fcmToken)
            val response = deviceTokenService.registerDeviceToken(request)

            if (response.isSuccessful) {
                Log.i("DeviceTokenRepository", "Token FCM registrado/actualizado en API exitosamente.")
                Result.success(Unit)
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Error desconocido"
                Log.e("DeviceTokenRepository", "API Error (${response.code()}) al registrar token FCM: $errorMsg")
                Result.failure(DeviceTokenException.ApiError(errorMsg, response.code()))
            }
        } catch (e: IOException) {
            Log.e("DeviceTokenRepository", "Error de red registrando token", e)
            Result.failure(DeviceTokenException.NetworkError(e))
        } catch (e: Exception) {
            Log.e("DeviceTokenRepository", "Excepción al registrar token FCM en API", e)
            Result.failure(DeviceTokenException.UnknownError(e))
        }
    }

    suspend fun deleteTokenFromApi(fcmToken: String): Result<Unit> {
        return try {
            Log.d("DeviceTokenRepository", "Intentando eliminar token FCM de API: ${fcmToken.take(10)}...")
            val response = deviceTokenService.deleteDeviceToken(fcmToken)

            if (response.isSuccessful || response.code() == 204) {
                Log.i("DeviceTokenRepository", "Token FCM eliminado de API exitosamente (o no encontrado).")
                Result.success(Unit)
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Error desconocido"
                Log.e("DeviceTokenRepository", "API Error (${response.code()}) al eliminar token FCM: $errorMsg")
                Result.failure(DeviceTokenException.ApiError(errorMsg, response.code()))
            }
        } catch (e: IOException) {
            Log.e("DeviceTokenRepository", "Error de red eliminando token", e)
            Result.failure(DeviceTokenException.NetworkError(e))
        } catch (e: Exception) {
            Log.e("DeviceTokenRepository", "Excepción al eliminar token FCM de API", e)
            Result.failure(DeviceTokenException.UnknownError(e))
        }
    }
}