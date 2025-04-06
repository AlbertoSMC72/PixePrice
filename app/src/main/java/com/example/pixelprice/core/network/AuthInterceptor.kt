package com.example.pixelprice.core.network

// import android.content.Context // Ya no es necesario
import android.util.Log
import com.example.pixelprice.core.data.TokenManager // Importar TokenManager
import okhttp3.Interceptor
import okhttp3.Response

// *** MODIFICADO: Ya no necesita Context ***
class AuthInterceptor : Interceptor { // Quitar parámetro del constructor

    override fun intercept(chain: Interceptor.Chain): Response {
        // *** MODIFICADO: Obtener token sin contexto ***
        val token = TokenManager.getToken()
        val originalRequest = chain.request()
        val requestBuilder = originalRequest.newBuilder()

        if (token != null) {
            Log.d("AuthInterceptor", "Añadiendo token JWT a la cabecera")
            requestBuilder.header("Authorization", "Bearer $token")
        } else {
            // Log opcional si quieres saber cuándo NO se añade token
            // Log.d("AuthInterceptor", "No hay token JWT para añadir.")
        }

        val request = requestBuilder.build()
        return chain.proceed(request)
    }
}