package com.example.pixelprice.features.projects.domain.usecases

import com.example.pixelprice.core.data.UserInfoProvider
import com.example.pixelprice.features.projects.data.local.ProjectEntity
import com.example.pixelprice.features.projects.data.repository.ProjectException
import com.example.pixelprice.features.projects.data.repository.ProjectRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class GetUserProjectsUseCase {
    private val repository = ProjectRepository()

    operator fun invoke(): Flow<List<ProjectEntity>> {
        if (UserInfoProvider.userID == 0) {
            return flow { throw ProjectException.NotAuthenticated() }
        }
        return repository.getUserProjects()
    }
}