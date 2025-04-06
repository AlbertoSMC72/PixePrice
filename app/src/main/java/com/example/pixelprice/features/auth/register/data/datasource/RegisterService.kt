package com.example.pixelprice.features.auth.register.data.datasource

import com.example.pixelprice.features.auth.register.data.model.CreateUserRequest
import com.example.pixelprice.features.auth.register.data.model.UserDTO
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface RegisterService {

    @POST("/users")
    suspend fun createUser(@Body request: CreateUserRequest): Response<UserDTO>
}