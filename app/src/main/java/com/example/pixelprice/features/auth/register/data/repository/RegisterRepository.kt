package com.example.pixelprice.features.auth.register.data.repository

import android.util.Log
import com.example.pixelprice.core.network.RetrofitHelper
import com.example.pixelprice.features.auth.login.data.repository.ApiException
import com.example.pixelprice.features.auth.register.data.datasource.RegisterService
import com.example.pixelprice.features.auth.register.data.model.CreateUserRequest
import com.example.pixelprice.features.auth.register.data.model.UserDTO
import java.io.IOException

class RegisterRepository {

    private val registerService: RegisterService by lazy {
        RetrofitHelper.createService(RegisterService::class.java)
    }

    suspend fun createUser(request: CreateUserRequest): Result<UserDTO> {
        return try {
            Log.d("RegisterRepository", "Intentando crear usuario: ${request.email}")
            val response = registerService.createUser(request)

            if (response.isSuccessful) {
                val createdUser = response.body()
                if (createdUser != null) {
                    Log.i("RegisterRepository", "Usuario creado exitosamente en API. ID: ${createdUser.id}")
                    Result.success(createdUser)
                } else {
                    Log.w("RegisterRepository", "Respuesta API OK (2xx), pero cuerpo nulo.")
                    Result.failure(ApiException.InvalidResponse("Respuesta de creación de usuario vacía."))
                }
            } else {
                val errorCode = response.code()
                val errorMsg = response.errorBody()?.string() ?: "Sin detalles"
                Log.w("RegisterRepository", "Error de API al crear usuario ($errorCode): $errorMsg")

                when (errorCode) {
                    400 -> Result.failure(RegisterException.BadRequest(errorMsg))
                    409 -> Result.failure(RegisterException.Conflict(errorMsg))
                    else -> Result.failure(ApiException.ServerError(errorCode, errorMsg))
                }
            }
        } catch (e: IOException) {
            Log.e("RegisterRepository", "Error de red durante la creación de usuario", e)
            Result.failure(RegisterException.NetworkError(e))
        } catch (e: Exception) {
            Log.e("RegisterRepository", "Excepción inesperada durante la creación de usuario", e)
            Result.failure(RegisterException.UnknownError(e))
        }
    }
}

sealed class RegisterException(message: String, cause: Throwable? = null) : Exception(message, cause) {
    class BadRequest(details: String, message: String = "Datos inválidos: $details") : RegisterException(message)
    class Conflict(details: String, message: String = "Conflicto: $details") : RegisterException(message)
    class NetworkError(cause: Throwable) : RegisterException("Error de red. Verifica tu conexión.", cause)
    class UnknownError(cause: Throwable) : RegisterException("Error inesperado al registrar.", cause)
}