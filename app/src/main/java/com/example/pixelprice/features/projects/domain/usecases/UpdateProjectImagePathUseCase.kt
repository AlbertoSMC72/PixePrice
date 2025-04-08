package com.example.pixelprice.features.projects.domain.usecases

import com.example.pixelprice.features.projects.data.repository.ProjectRepository

class UpdateProjectImagePathUseCase {
    private val repository = ProjectRepository()

    suspend operator fun invoke(projectId: Int, imagePath: String?): Result<Unit> {
        if (projectId <= 0) {
            return Result.failure(IllegalArgumentException("ID de proyecto inválido."))
        }
        return repository.updateProjectImageUri(projectId, imagePath)
    }
}