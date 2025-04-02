package com.example.pixelprice.features.views.projectDetail.domain

import com.example.pixelprice.features.views.createProject.data.model.ProjectDTO
import com.example.pixelprice.features.views.projectDetail.data.repository.ProjectRepository


class GetProjectUseCase {
    private val repository = ProjectRepository()

    suspend operator fun invoke(id: Int): Result<ProjectDTO> {
        return repository.getProjectById(id)
    }
}
