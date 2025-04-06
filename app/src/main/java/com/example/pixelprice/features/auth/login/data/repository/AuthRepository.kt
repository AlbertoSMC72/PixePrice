package com.example.pixelprice.features.auth.login.data.repository

import android.util.Log
import com.example.pixelprice.core.data.TokenManager
import com.example.pixelprice.core.data.UserInfoProvider
import com.example.pixelprice.core.network.RetrofitHelper
import com.example.pixelprice.features.auth.login.data.datasource.AuthService
import com.example.pixelprice.features.auth.login.data.model.LoginRequest
import com.example.pixelprice.features.auth.login.data.model.LoginResponse
import java.io.IOException

class AuthRepository {

    private val authService: AuthService by lazy {
        RetrofitHelper.createService(AuthService::class.java)
    }

    // Retorna Result<Unit> en éxito, o Result.Failure con excepción específica
    suspend fun login(email: String, password: String): Result<Unit> { // Devolver Unit en éxito es más simple
        return try {
            Log.d("AuthRepository", "Iniciando login para: $email")
            val response = authService.login(LoginRequest(email, password))

            if (response.isSuccessful) {
                val loginData: LoginResponse? = response.body()
                if (loginData?.token != null && loginData.user != null) {
                    Log.i("AuthRepository", "Login API exitoso para usuario ID: ${loginData.user.id}. Guardando datos.")

                    TokenManager.saveAuthData(
                        token = loginData.token,
                        userId = loginData.user.id,
                        username = loginData.user.username // Pasar el nombre de usuario también
                    )
                    // Opcional: Actualizar UserInfoProvider si todavía se usa en memoria en algún lado
                    UserInfoProvider.setUserInfo(loginData.user.id, loginData.user.username)

                    Result.success(Unit)
                } else {
                    // Respuesta exitosa (2xx) pero cuerpo inválido o datos faltantes
                    Log.w("AuthRepository", "Respuesta API OK (2xx), pero cuerpo/token/usuario nulo. Body: $loginData")
                    Result.failure(ApiException.InvalidResponse()) // Excepción personalizada
                }
            } else {
                // Error de API (4xx, 5xx)
                val errorCode = response.code()
                val errorMsg = response.errorBody()?.string() ?: "Sin detalles del error"
                Log.w("AuthRepository", "Error de API en login ($errorCode): $errorMsg")

                // Mapear a excepciones específicas si es posible
                when (errorCode) {
                    401 -> Result.failure(AuthException.InvalidCredentials())
                    // Puedes añadir otros códigos comunes (400 Bad Request, 404 Not Found, etc.)
                    else -> Result.failure(ApiException.ServerError(errorCode, errorMsg))
                }
            }
        } catch (e: IOException) {
            // Error de Red (ConnectException, SocketTimeoutException, etc.)
            Log.e("AuthRepository", "Error de red durante el login", e)
            Result.failure(AuthException.NetworkError(e))
        } catch (e: Exception) {
            // Otro error inesperado (ej. JSON malformado, etc.)
            Log.e("AuthRepository", "Excepción inesperada durante el login", e)
            Result.failure(AuthException.UnknownError(e))
        }
    }
}

// --- Opcional: Definir excepciones personalizadas para mejor manejo en ViewModel ---
sealed class AuthException(message: String, cause: Throwable? = null) : Exception(message, cause) {
    class InvalidCredentials(message: String = "Credenciales inválidas.") : AuthException(message)
    class NetworkError(cause: Throwable) : AuthException("Error de red. Verifica tu conexión.", cause)
    class UnknownError(cause: Throwable) : AuthException("Error inesperado durante el login.", cause)
}
sealed class ApiException(message: String, val code: Int? = null) : Exception(message) {
    class ServerError(code: Int, details: String) : ApiException("Error del servidor ($code): $details", code)
    class InvalidResponse(message: String = "Respuesta inválida del servidor.") : ApiException(message)
    // Podrías añadir ClientError(code, details) para 4xx genéricos
}