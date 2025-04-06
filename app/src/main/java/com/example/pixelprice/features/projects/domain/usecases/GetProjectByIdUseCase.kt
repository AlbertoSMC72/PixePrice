package com.example.pixelprice.features.projects.domain.usecases

import com.example.pixelprice.features.projects.data.local.ProjectEntity
import com.example.pixelprice.features.projects.data.repository.ProjectRepository

class GetProjectByIdUseCase {
    private val repository = ProjectRepository()

    suspend operator fun invoke(projectId: Int): Result<ProjectEntity> {
        if (projectId <= 0) {
            return Result.failure(IllegalArgumentException("ID de proyecto inválido."))
        }
        return repository.getProjectById(projectId)
    }
}