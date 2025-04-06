package com.example.pixelprice.features.projects.data.repository

import android.database.sqlite.SQLiteConstraintException
import android.util.Log
import com.example.pixelprice.core.data.UserInfoProvider
import com.example.pixelprice.core.local.DatabaseProvider
import com.example.pixelprice.features.projects.data.local.ProjectDao
import com.example.pixelprice.features.projects.data.local.ProjectEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ProjectRepository {

    // Obtiene el DAO de forma diferida
    private val projectDao: ProjectDao by lazy {
        DatabaseProvider.getAppDataBase().projectDao()
    }

    // Ejecuta operaciones DAO en el hilo IO
    private suspend fun <T> dbCall(block: suspend (ProjectDao) -> T): Result<T> = withContext(Dispatchers.IO) {
        try {
            Result.success(block(projectDao))
        } catch (e: SQLiteConstraintException) {
            Log.w("ProjectRepository", "Constraint violation: ${e.message}")
            // Específicamente útil para detectar nombres duplicados en insert
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
    ): Result<Long> { // Devuelve el ID del proyecto creado
        val userId = UserInfoProvider.userID
        if (userId == 0) return Result.failure(ProjectException.NotAuthenticated())

        // Comprobación de nombre duplicado (alternativa a esperar la excepción)
        // val nameTaken = dbCall { it.isProjectNameTaken(name) }.getOrNull() == true
        // if (nameTaken) {
        //     return Result.failure(ProjectException.NameAlreadyExists("El nombre del proyecto ya existe."))
        // }

        val project = ProjectEntity(
            userId = userId,
            name = name,
            description = description,
            capital = capital,
            isSelfMade = isSelfMade,
            createdAt = System.currentTimeMillis() // Asegura timestamp fresco
        )
        return dbCall { dao -> dao.insertProject(project) }
    }

    suspend fun getUserProjects(): Result<List<ProjectEntity>> {
        val userId = UserInfoProvider.userID
        if (userId == 0) return Result.failure(ProjectException.NotAuthenticated())
        return dbCall { dao -> dao.getAllProjectsByUserId(userId) }
    }

    suspend fun getProjectById(projectId: Int): Result<ProjectEntity> {
        val userId = UserInfoProvider.userID
        if (userId == 0) return Result.failure(ProjectException.NotAuthenticated())
        return dbCall { dao ->
            dao.getProjectByIdAndUserId(projectId, userId)
                ?: throw ProjectException.NotFound("Proyecto no encontrado con ID: $projectId")
        }
    }

    suspend fun isProjectNameUnique(name: String): Result<Boolean> {
        // Esta función podría no ser necesaria si la verificación se hace en createProject
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

    suspend fun deleteProject(projectId: Int): Result<Int> { // Devuelve número de filas eliminadas
        val userId = UserInfoProvider.userID
        if (userId == 0) return Result.failure(ProjectException.NotAuthenticated())
        return dbCall { dao -> dao.deleteProjectByIdAndUserId(projectId, userId) }
    }
}

// Excepciones específicas para la gestión de proyectos locales
sealed class ProjectException(message: String, cause: Throwable? = null) : Exception(message, cause) {
    class DatabaseError(message: String, cause: Throwable) : ProjectException(message, cause)
    class NameAlreadyExists(message: String) : ProjectException(message)
    class NotFound(message: String) : ProjectException(message)
    class NotAuthenticated(message: String = "Usuario no autenticado") : ProjectException(message)
}