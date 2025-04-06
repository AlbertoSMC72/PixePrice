package com.example.pixelprice.features.projects.domain.usecases

import com.example.pixelprice.core.data.UserInfoProvider
import com.example.pixelprice.features.projects.data.local.ProjectEntity
import com.example.pixelprice.features.projects.data.repository.ProjectException
import com.example.pixelprice.features.projects.data.repository.ProjectRepository
import kotlinx.coroutines.flow.Flow // *** IMPORTAR Flow ***
import kotlinx.coroutines.flow.flow

class GetUserProjectsUseCase {
    private val repository = ProjectRepository()

    // *** CAMBIO: Devolver Flow y quitar suspend ***
    operator fun invoke(): Flow<List<ProjectEntity>> {
        // Puedes añadir validación de UserID aquí si prefieres
        if (UserInfoProvider.userID == 0) {
            // Retornar un Flow que emita un error o una lista vacía
            return flow { throw ProjectException.NotAuthenticated() }
            // O: return flowOf(emptyList())
        }
        return repository.getUserProjects()
    }
}