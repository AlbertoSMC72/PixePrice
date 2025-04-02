package com.example.pixelprice.features.views.createProject.data.model

data class ProjectDTO(
    val id: Int,
    val name: String,
    val description: String,
    val capital: Double,
    val isSelfMade: Boolean,
    val createdAt: String
)
