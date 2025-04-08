package com.example.pixelprice.features.projects.data.repository

import android.database.sqlite.SQLiteConstraintException
import android.util.Log
import com.example.pixelprice.core.data.UserInfoProvider
import com.example.pixelprice.core.local.DatabaseProvider
import com.example.pixelprice.features.projects.data.local.ProjectDao
import com.example.pixelprice.features.projects.data.local.ProjectEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn

class ProjectRepository {

    private val projectDao: ProjectDao by lazy {
        DatabaseProvider.getAppDataBase().projectDao()
    }

    private suspend fun <T> dbCall(block: suspend (ProjectDao) -> T): Result<T> = withContext(Dispatchers.IO) {
        try {
            Result.success(block(projectDao))
        } catch (e: SQLiteConstraintException) {
            Log.w("ProjectRepository", "Constraint violation: ${e.message}")
            Result.failure(ProjectException.NameAlreadyExists("El nombre del proyecto ya existe."))
        } catch (e: Exception) {
            Log.e("ProjectRepository", "Database error: ${e.message}", e)
            Result.failure(ProjectException.DatabaseError("Error en la base de datos: ${e.message}", e))
        }
    }

    suspend fun createProject(
        name: String,
        description: String,
        capital: Double,
        isSelfMade: Boolean
    ): Result<Long> {
        val userId = UserInfoProvider.userID
        if (userId == 0) return Result.failure(ProjectException.NotAuthenticated())


        val project = ProjectEntity(
            userId = userId,
            name = name,
            description = description,
            capital = capital,
            isSelfMade = isSelfMade,
            createdAt = System.currentTimeMillis()
        )
        return dbCall { dao -> dao.insertProject(project) }
    }

    fun getUserProjects(): Flow<List<ProjectEntity>> {
        val userId = UserInfoProvider.userID
        Log.d("ProjectRepository", "Obteniendo Flow de proyectos para userId: $userId")
        return projectDao.getAllProjectsByUserId(userId)
            .catch { e ->
                Log.e("ProjectRepository", "Error en Flow de getUserProjects", e)
                emit(emptyList())
            }
            .flowOn(Dispatchers.IO)
    }

    suspend fun getProjectById(projectId: Int): Result<ProjectEntity> {
        val userId = UserInfoProvider.userID
        if (userId == 0) return Result.failure(ProjectException.NotAuthenticated())
        return dbCall { dao ->
            dao.getProjectByIdAndUserId(projectId, userId)
                ?: throw ProjectException.NotFound("Proyecto no encontrado con ID: $projectId")
        }
    }

    suspend fun getProjectByName(projectName: String): Result<ProjectEntity> {
        val userId = UserInfoProvider.userID
        if (userId == 0) return Result.failure(ProjectException.NotAuthenticated())
        return dbCall { dao ->
            dao.getProjectByNameAndUserId(projectName, userId)
                ?: throw ProjectException.NotFound("Proyecto no encontrado con nombre: $projectName")
        }
    }

    suspend fun isProjectNameUnique(name: String): Result<Boolean> {
        return dbCall { dao -> !dao.isProjectNameTaken(name) }
    }

    suspend fun updateProjectImageUri(projectId: Int, imageUri: String?): Result<Unit> {
        val userId = UserInfoProvider.userID
        if (userId == 0) return Result.failure(ProjectException.NotAuthenticated())
        return dbCall { dao -> dao.updateProjectImageUri(projectId, userId, imageUri) }
    }

    suspend fun updateProjectQuotationStatus(projectId: Int, quotationId: Int?, isPending: Boolean): Result<Unit> {
        val userId = UserInfoProvider.userID
        if (userId == 0) return Result.failure(ProjectException.NotAuthenticated())
        return dbCall { dao -> dao.updateProjectQuotationStatus(projectId, userId, quotationId, isPending) }
    }

    suspend fun deleteProject(projectId: Int): Result<Int> {
        val userId = UserInfoProvider.userID
        if (userId == 0) return Result.failure(ProjectException.NotAuthenticated())
        return dbCall { dao -> dao.deleteProjectByIdAndUserId(projectId, userId) }
    }
}

sealed class ProjectException(message: String, cause: Throwable? = null) : Exception(message, cause) {
    class DatabaseError(message: String, cause: Throwable) : ProjectException(message, cause)
    class NameAlreadyExists(message: String) : ProjectException(message)
    class NotFound(message: String) : ProjectException(message)
    class NotAuthenticated(message: String = "Usuario no autenticado") : ProjectException(message)
}