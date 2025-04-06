package com.example.pixelprice.features.profile.data.model

import com.google.gson.annotations.SerializedName

data class ProfileDTO(
    @SerializedName("id") // Asumiendo que la API devuelve el ID
    val id: Int,

    @SerializedName("email")
    val email: String, // Email (solo lectura en UI)

    @SerializedName("name")
    val name: String?, // Puede ser nulo

    @SerializedName("last_name")
    val lastName: String?, // Puede ser nulo

    @SerializedName("created_at") // Opcional, si la API lo devuelve
    val createdAt: String? = null // Como String o parsear a Date/LocalDateTime
)