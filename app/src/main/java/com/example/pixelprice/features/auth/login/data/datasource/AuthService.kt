package com.example.pixelprice.features.auth.login.data.datasource

import com.example.pixelprice.features.auth.login.data.model.LoginRequest
import com.example.pixelprice.features.auth.login.data.model.LoginResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthService {
    @POST("/users/auth/login")
    suspend fun login(@Body loginRequest: LoginRequest): Response<LoginResponse>
}