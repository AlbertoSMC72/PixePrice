package com.example.pixelprice.features.views.createProject.domain

import com.example.pixelprice.features.views.createProject.data.model.CreateProjectRequest
import com.example.pixelprice.features.views.createProject.data.model.ProjectDTO
import com.example.pixelprice.features.views.createProject.data.repository.ProjectRepository

class CreateProjectUseCase {
    private val repository = ProjectRepository()

    suspend operator fun invoke(request: CreateProjectRequest): Result<ProjectDTO> {
        return repository.createProject(request)
    }
}
