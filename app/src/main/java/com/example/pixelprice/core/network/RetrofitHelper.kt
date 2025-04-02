package com.example.pixelprice.core.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitHelper {

    private const val BASE_URL = "http://52.72.192.19:3000/"

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // Ejemplo de método genérico si quisieras usarlo en otros lados
    fun <T> createService(service: Class<T>): T {
        return retrofit.create(service)
    }
}