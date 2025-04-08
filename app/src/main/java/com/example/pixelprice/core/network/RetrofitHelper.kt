package com.example.pixelprice.core.network

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import androidx.annotation.VisibleForTesting

object RetrofitHelper {
    private const val BASE_URL = "http://18.205.137.128:8080"

    @SuppressLint("StaticFieldLeak")
    private lateinit var retrofitInstance: Retrofit
    private var isInitialized = false
    private val lock = Any()

    fun initialize(context: Context) {
        if (!isInitialized) {
            synchronized(lock) {
                if (!isInitialized) {
                    val loggingInterceptor = HttpLoggingInterceptor().apply {
                        level = HttpLoggingInterceptor.Level.BODY
                    }

                    val authInterceptor = AuthInterceptor()

                    val okHttpClient = OkHttpClient.Builder()
                        .addInterceptor(authInterceptor)
                        .addInterceptor(loggingInterceptor)
                        .connectTimeout(30, TimeUnit.SECONDS)
                        .readTimeout(30, TimeUnit.SECONDS)
                        .writeTimeout(30, TimeUnit.SECONDS)
                        .build()

                    retrofitInstance = Retrofit.Builder()
                        .baseUrl(BASE_URL)
                        .client(okHttpClient)
                        .addConverterFactory(GsonConverterFactory.create())
                        .build()

                    isInitialized = true
                    Log.d("RetrofitHelper", "RetrofitHelper Inicializado.")
                }
            }
        }
    }

    fun <T> createService(serviceClass: Class<T>): T {
        check(isInitialized) { "RetrofitHelper no ha sido inicializado. Llama a initialize() en Application.onCreate." }
        return retrofitInstance.create(serviceClass)
    }

    fun getRetrofitInstance(): Retrofit {
        check(isInitialized) { "RetrofitHelper no ha sido inicializado." }
        return retrofitInstance
    }

    @VisibleForTesting
    internal fun resetForTest() {
        synchronized(lock) {
            isInitialized = false
        }
    }
}