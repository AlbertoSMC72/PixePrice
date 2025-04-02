package com.example.pixelprice.features.authorization.login.data.datasource

import com.example.pixelprice.features.authorization.login.data.model.LoginRequest
import com.example.pixelprice.features.authorization.login.data.model.UserResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthService {
    @POST("users/login")
    suspend fun login(@Body loginRequest: LoginRequest): Response<UserResponse>
}
