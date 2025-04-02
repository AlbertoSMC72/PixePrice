package com.example.pixelprice.features.views.projectDetail.data.repository

import com.example.pixelprice.core.network.RetrofitHelper
import com.example.pixelprice.features.views.createProject.data.model.ProjectDTO
import com.example.pixelprice.features.views.projectDetail.data.datasourse.ProjectService

class ProjectRepository {

    private val service = RetrofitHelper.createService(ProjectService::class.java)

    suspend fun getProjectById(id: Int): Result<ProjectDTO> = try {
        val response = service.getProjectById(id)
        if (response.isSuccessful) Result.success(response.body()!!)
        else Result.failure(Exception("Error ${response.code()}"))
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun requestQuote(id: Int): Result<Unit> = try {
        val response = service.requestQuote(id)
        if (response.isSuccessful) Result.success(Unit)
        else Result.failure(Exception("Error ${response.code()}"))
    } catch (e: Exception) {
        Result.failure(e)
    }
}
