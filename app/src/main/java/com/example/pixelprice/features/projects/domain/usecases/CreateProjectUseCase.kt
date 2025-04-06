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
    ): Result<Long> { // Devuelve el ID del proyecto creado
        // Validación básica
        if (name.isBlank() || description.isBlank() || capital < 0) {
            return Result.failure(IllegalArgumentException("Datos del proyecto inválidos."))
        }
        // La comprobación de unicidad y la creación se manejan en el repositorio
        return repository.createProject(name, description, capital, isSelfMade)
    }
}