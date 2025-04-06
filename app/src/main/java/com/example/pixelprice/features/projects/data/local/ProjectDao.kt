package com.example.pixelprice.features.projects.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ProjectDao {

    @Insert(onConflict = OnConflictStrategy.ABORT) // Abortar si el nombre (unique) ya existe
    suspend fun insertProject(project: ProjectEntity): Long // Devuelve el ID insertado

    @Query("SELECT * FROM projects WHERE user_id = :userId ORDER BY created_at DESC")
    fun getAllProjectsByUserId(userId: Int): Flow<List<ProjectEntity>>

    @Query("SELECT * FROM projects WHERE id = :projectId AND user_id = :userId LIMIT 1")
    suspend fun getProjectByIdAndUserId(projectId: Int, userId: Int): ProjectEntity?

    @Query("SELECT * FROM projects WHERE name = :projectName AND user_id = :userId LIMIT 1")
    suspend fun getProjectByNameAndUserId(projectName: String, userId: Int): ProjectEntity?

    @Query("SELECT EXISTS(SELECT 1 FROM projects WHERE name = :name LIMIT 1)")
    suspend fun isProjectNameTaken(name: String): Boolean

    // Actualiza solo campos específicos (ej. URI de imagen, ID de cotización)
    @Update
    suspend fun updateProject(project: ProjectEntity)

    // Método específico para actualizar solo el URI de la imagen
    @Query("UPDATE projects SET image_uri = :imageUri WHERE id = :projectId AND user_id = :userId")
    suspend fun updateProjectImageUri(projectId: Int, userId: Int, imageUri: String?)

    // Método específico para actualizar estado de cotización
    @Query("UPDATE projects SET last_quotation_id = :quotationId, has_pending_quotation = :isPending WHERE id = :projectId AND user_id = :userId")
    suspend fun updateProjectQuotationStatus(projectId: Int, userId: Int, quotationId: Int?, isPending: Boolean)

    @Query("DELETE FROM projects WHERE id = :projectId AND user_id = :userId")
    suspend fun deleteProjectByIdAndUserId(projectId: Int, userId: Int): Int // Devuelve número de filas eliminadas
}