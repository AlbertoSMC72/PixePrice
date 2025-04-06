package com.example.pixelprice.features.projects.data.repository

import android.database.sqlite.SQLiteConstraintException
import android.util.Log
import com.example.pixelprice.core.data.UserInfoProvider
import com.example.pixelprice.core.local.DatabaseProvider
import com.example.pixelprice.features.projects.data.local.ProjectDao
import com.example.pixelprice.features.projects.data.local.ProjectEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.Flow // *** IMPORTAR Flow ***
import kotlinx.coroutines.flow.catch // Opcional para manejo de errores
import kotlinx.coroutines.flow.flowOn // Opcional para asegurar hilo

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

    fun getUserProjects(): Flow<List<ProjectEntity>> { // <-- Cambia tipo y quita suspend
        val userId = UserInfoProvider.userID
        // Validar userId aquí podría ser complicado con Flow, mejor manejarlo en ViewModel o UseCase
        // if (userId == 0) return flowOf(emptyList()) // O emitir un error
        Log.d("ProjectRepository", "Obteniendo Flow de proyectos para userId: $userId")
        // Simplemente devuelve el Flow del DAO. Room se encarga del hilo.
        return projectDao.getAllProjectsByUserId(userId)
            // Opcional: Añadir manejo de errores en el Flow si Room lanza alguna excepción rara
            .catch { e ->
                Log.e("ProjectRepository", "Error en Flow de getUserProjects", e)
                emit(emptyList()) // Emitir lista vacía en caso de error, o relanzar
            }
            // Opcional: Asegurar que se observa en IO, aunque Room suele manejarlo
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

    suspend fun getProjectByName(projectName: String): Result<ProjectEntity> { // <-- Cambiar projectId por projectName
        val userId = UserInfoProvider.userID
        if (userId == 0) return Result.failure(ProjectException.NotAuthenticated())
        // Llama al método DAO con los parámetros correctos
        return dbCall { dao ->
            dao.getProjectByNameAndUserId(projectName, userId)
                ?: throw ProjectException.NotFound("Proyecto no encontrado con nombre: $projectName") // <-- Mensaje de error actualizado
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