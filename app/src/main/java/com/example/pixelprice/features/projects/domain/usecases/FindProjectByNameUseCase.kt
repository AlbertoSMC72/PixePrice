package com.example.pixelprice.features.projects.domain.usecases

import com.example.pixelprice.features.projects.data.local.ProjectEntity
import com.example.pixelprice.features.projects.data.repository.ProjectRepository
import com.example.pixelprice.features.projects.data.repository.ProjectException // Importar excepción

// Caso de uso para buscar un proyecto por su nombre (necesario para actualizar desde notificación)
class FindProjectByNameUseCase {
    private val repository = ProjectRepository()

    suspend operator fun invoke(projectName: String): Result<ProjectEntity> {
        if (projectName.isBlank()) {
            return Result.failure(IllegalArgumentException("El nombre del proyecto no puede estar vacío."))
        }
        // El repositorio necesita un método para buscar por nombre (lo añadiremos)
        // Por ahora, simulamos buscando todos y filtrando (¡ineficiente!)
        // TODO: Añadir `getProjectByName` al DAO y Repositorio
        val allProjectsResult = repository.getUserProjects()
        return allProjectsResult.mapCatching { projects ->
            projects.firstOrNull { it.name.equals(projectName, ignoreCase = true) }
                ?: throw ProjectException.NotFound("Proyecto no encontrado con nombre: $projectName")
        }
    }
}