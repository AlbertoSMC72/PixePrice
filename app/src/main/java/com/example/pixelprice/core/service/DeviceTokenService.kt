package com.example.pixelprice.core.service

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.POST
import retrofit2.http.Path

// DTO para el cuerpo de la solicitud de registro
data class RegisterTokenRequest(val token: String, val deviceType: String = "android")

// *** Interfaz para los endpoints de Device Token ***
interface DeviceTokenService {
    // Endpoint para registrar el token (protegido por JWT en la API)
    @POST("/device-tokens")
    suspend fun registerDeviceToken(@Body request: RegisterTokenRequest): Response<Unit> // API devuelve 201/200

    // Endpoint para eliminar el token (protegido por JWT en la API)
    @DELETE("/device-tokens/{token}") // El token a borrar va en la URL
    suspend fun deleteDeviceToken(@Path("token") fcmToken: String): Response<Unit> // API devuelve 204
}