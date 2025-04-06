package com.example.pixelprice.features.quotations.data.remote.model

import com.google.gson.annotations.SerializedName

// DTO para la lista de cotizaciones obtenida de la API
data class QuotationListItemDTO(
    @SerializedName("id")
    val id: Int,

    @SerializedName("name") // Asume que la API devuelve el nombre del proyecto aquí
    val projectName: String,

    @SerializedName("createdAt") // Fecha de creación de la cotización
    val createdAt: String, // O parsear a Date/LocalDateTime si es necesario

    @SerializedName("status") // Estado de la cotización (ej. "PENDING", "READY", "ERROR")
    val status: String? // Nullable por si la API no siempre lo envía
)