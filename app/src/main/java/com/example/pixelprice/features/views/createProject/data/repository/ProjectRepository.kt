package com.example.pixelprice.features.views.createProject.data.repository

import com.example.pixelprice.core.network.RetrofitHelper
import com.example.pixelprice.features.views.createProject.data.datasourse.ProjectService
import com.example.pixelprice.features.views.createProject.data.model.CreateProjectRequest
import com.example.pixelprice.features.views.createProject.data.model.ProjectDTO

class ProjectRepository {

    private val service = RetrofitHelper.createService(ProjectService::class.java)

    suspend fun createProject(request: CreateProjectRequest): Result<ProjectDTO> = try {
        val response = service.createProject(request)
        if (response.isSuccessful) Result.success(response.body()!!)
        else Result.failure(Exception(response.errorBody()?.string()))
    } catch (e: Exception) {
        Result.failure(e)
    }
}
