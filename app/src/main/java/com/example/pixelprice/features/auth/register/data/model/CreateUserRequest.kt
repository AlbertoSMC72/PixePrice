package com.example.pixelprice.features.auth.register.data.model

data class CreateUserRequest(
    val email: String,
    val password: String,
    val firebaseToken: String?,
    val name: String?,
    val last_name: String?
)