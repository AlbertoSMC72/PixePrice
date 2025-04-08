package com.example.pixelprice.core.service

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.POST
import retrofit2.http.Path

data class RegisterTokenRequest(val token: String, val deviceType: String = "android")

interface DeviceTokenService {
    @POST("/device-tokens")
    suspend fun registerDeviceToken(@Body request: RegisterTokenRequest): Response<Unit>

    @DELETE("/device-tokens/{token}")
    suspend fun deleteDeviceToken(@Path("token") fcmToken: String): Response<Unit>
}