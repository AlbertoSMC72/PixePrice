package com.example.pixelprice.features.projects.domain.usecases

import com.example.pixelprice.features.projects.data.repository.ProjectRepository
import com.example.pixelprice.features.projects.data.repository.ProjectException

class CreateProjectUseCase {
    private val repository = ProjectRepository()

    suspend operator fun invoke(
        name: String,
        description: String,
        capital: Double,
        isSelfMade: Boolean
    ): Result<Long> {
        if (name.isBlank() || description.isBlank() || capital < 0) {
            return Result.failure(IllegalArgumentException("Datos del proyecto inválidos."))
        }
        return repository.createProject(name, description, capital, isSelfMade)
    }
}