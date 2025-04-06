package com.example.pixelprice.features.projects.domain.usecases

import com.example.pixelprice.features.projects.data.local.ProjectEntity
import com.example.pixelprice.features.projects.data.repository.ProjectRepository

class GetUserProjectsUseCase {
    private val repository = ProjectRepository()

    suspend operator fun invoke(): Result<List<ProjectEntity>> {
        return repository.getUserProjects()
    }
}