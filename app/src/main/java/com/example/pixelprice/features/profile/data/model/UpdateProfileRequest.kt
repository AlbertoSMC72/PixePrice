package com.example.pixelprice.features.profile.data.model

import com.google.gson.annotations.SerializedName

// DTO para el cuerpo de la solicitud PUT /users/{id}
// Ajustado a los campos editables y la entidad User de tu API
data class UpdateProfileRequest(
    // No incluimos email si no se permite cambiar aquí
    // No incluimos username si no existe en la API

    @SerializedName("name")
    val name: String?, // Enviar null o string vacío si el usuario lo borra

    @SerializedName("last_name")
    val lastName: String? // Enviar null o string vacío si el usuario lo borra
)
