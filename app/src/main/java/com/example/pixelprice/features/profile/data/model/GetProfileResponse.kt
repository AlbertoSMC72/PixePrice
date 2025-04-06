package com.example.pixelprice.features.profile.data.model

import com.google.gson.annotations.SerializedName

// Modelo que representa la respuesta COMPLETA de la API GET /users/{id} y PATCH /users/{id}
data class GetProfileResponse(
    @SerializedName("status")
    val status: String,

    @SerializedName("message")
    val message: String,

    // El campo "data" contiene el objeto "user"
    @SerializedName("data")
    val data: ProfileData? // Puede ser nulo si hay error
)

// Contenedor intermedio "data"
data class ProfileData(
    @SerializedName("user")
    val user: UserResponseInfo? // El objeto de usuario real
)


// Modelo para la información del usuario devuelta DENTRO de "user"
data class UserResponseInfo(
    @SerializedName("id")
    val id: Int,

    // Asegurar nullabilidad y SerializedName correcto
    @SerializedName("name")
    val name: String?,

    @SerializedName("last_name")
    val lastName: String?,

    @SerializedName("email")
    val email: String
    // Añade otros campos si la API los devuelve aquí (created_at, etc.)
)