package com.example.pixelprice.features.projects.domain.usecases

import com.example.pixelprice.features.projects.data.local.ProjectEntity
import com.example.pixelprice.features.projects.data.repository.ProjectRepository
import com.example.pixelprice.features.projects.data.repository.ProjectException

// Caso de uso para buscar un proyecto por su nombre
class FindProjectByNameUseCase {
    private val repository = ProjectRepository()

    suspend operator fun invoke(projectName: String): Result<ProjectEntity> {
        // Validación básica
        if (projectName.isBlank()) {
            return Result.failure(IllegalArgumentException("El nombre del proyecto no puede estar vacío."))
        }

        // *** LLAMAR AL MÉTODO getProjectByName DEL REPOSITORIO ***
        return repository.getProjectByName(projectName)
    }
}