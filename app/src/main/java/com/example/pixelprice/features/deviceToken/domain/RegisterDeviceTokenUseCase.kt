package com.example.pixelprice.features.deviceToken.domain

// import android.content.Context // Ya no es necesario
import com.example.pixelprice.features.deviceToken.data.DeviceTokenRepository

// *** CORREGIDO: No necesita Context ***
class RegisterDeviceTokenUseCase {
    // *** CORREGIDO: Instanciar sin contexto ***
    private val repository = DeviceTokenRepository()

    suspend operator fun invoke(fcmToken: String): Result<Unit> {
        if(fcmToken.isBlank()){
            return Result.failure(IllegalArgumentException("FCM Token no puede estar vacío."))
        }
        return repository.registerTokenWithApi(fcmToken)
    }
}