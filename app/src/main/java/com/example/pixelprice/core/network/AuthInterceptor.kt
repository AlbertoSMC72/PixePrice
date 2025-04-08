package com.example.pixelprice.core.network

import android.util.Log
import com.example.pixelprice.core.data.TokenManager
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val token = TokenManager.getToken()
        val originalRequest = chain.request()
        val requestBuilder = originalRequest.newBuilder()

        if (token != null) {
            Log.d("AuthInterceptor", "Añadiendo token JWT a la cabecera")
            requestBuilder.header("Authorization", "Bearer $token")
        } else {
        }

        val request = requestBuilder.build()
        return chain.proceed(request)
    }
}