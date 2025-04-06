package com.example.pixelprice.features.auth.login.data.model

import com.google.gson.annotations.SerializedName // Útil si los nombres JSON difieren

// *** DTO que mapea la respuesta JSON del login de la API ***
data class LoginResponse(
    @SerializedName("status") // Coincide con el JSON de la API
    val status: String,

    @SerializedName("message") // Coincide con el JSON de la API
    val message: String,

    @SerializedName("token") // Coincide con el JSON de la API (el JWT)
    val token: String?, // Házlo nullable por si la API no lo envía en caso de error

    // *** AÑADIR: Datos del usuario que la API DEBE devolver en el éxito ***
    // Esto es crucial para guardar el ID y nombre del usuario.
    // Ajusta los campos según lo que realmente devuelva tu API.
    @SerializedName("user") // Asume que la API devuelve un objeto 'user'
    val user: UserLoginInfo? // Puede ser nulo si el login falla
)

// *** DTO para la información del usuario devuelta en el login ***
data class UserLoginInfo(
    @SerializedName("id") // El ID del usuario de la base de datos
    val id: Int,

    @SerializedName("username") // El nombre de usuario
    val username: String,

    @SerializedName("email") // El email (opcional si ya lo tienes)
    val email: String
    // Añade otros campos si la API los devuelve (name, last_name, etc.)
)