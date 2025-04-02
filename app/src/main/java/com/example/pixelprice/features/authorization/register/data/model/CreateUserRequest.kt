package com.example.pixelprice.features.authorization.register.data.model

data class CreateUserRequest(
    val username: String,
    val email: String,
    val password: String,
    val firebaseToken: String?
)