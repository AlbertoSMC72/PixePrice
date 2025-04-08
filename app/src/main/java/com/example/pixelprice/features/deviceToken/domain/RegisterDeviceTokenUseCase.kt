package com.example.pixelprice.features.deviceToken.domain

import com.example.pixelprice.features.deviceToken.data.DeviceTokenRepository

class RegisterDeviceTokenUseCase {
    private val repository = DeviceTokenRepository()

    suspend operator fun invoke(fcmToken: String): Result<Unit> {
        if(fcmToken.isBlank()){
            return Result.failure(IllegalArgumentException("FCM Token no puede estar vacío."))
        }
        return repository.registerTokenWithApi(fcmToken)
    }
}