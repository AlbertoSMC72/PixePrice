package com.example.pixelprice.features.views.createProject.data.model

data class CreateProjectRequest(
    val userId: Int,
    val name: String,
    val description: String,
    val capital: Double,
    val isSelfMade: Boolean
)
