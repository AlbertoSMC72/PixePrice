package com.example.pixelprice.features.views.home.data.repository

import com.example.pixelprice.core.network.RetrofitHelper
import com.example.pixelprice.features.views.home.data.datasourse.ProjectService
import com.example.pixelprice.features.views.home.data.model.ProjectDTO

class HomeRepository {
    private val service = RetrofitHelper.createService(ProjectService::class.java)

    suspend fun getUserProjects(userId: Int): Result<List<ProjectDTO>> = try {
        val response = service.getProjectsByUser(userId)
        if (response.isSuccessful) Result.success(response.body()!!)
        else Result.failure(Exception(response.errorBody()?.string()))
    } catch (e: Exception) {
        Result.failure(e)
    }
}
