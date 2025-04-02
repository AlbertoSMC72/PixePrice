package com.example.pixelprice.features.views.quoteResult.data.model

data class QuoteDTO(
    val id: Int,
    val projectId: Int,
    val projectName: String,
    val summary: String,
    val estimatedCost: Double,
    val createdAt: String
)
