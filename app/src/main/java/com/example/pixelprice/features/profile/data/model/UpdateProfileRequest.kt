package com.example.pixelprice.features.profile.data.model

import com.google.gson.annotations.SerializedName

// DTO para el cuerpo de la solicitud PATCH /users/{id}
data class UpdateProfileRequest(
    @SerializedName("name")
    val name: String?, // Enviar null o string vacío si el usuario lo borra

    @SerializedName("last_name")
    val lastName: String? // Enviar null o string vacío si el usuario lo borra
)