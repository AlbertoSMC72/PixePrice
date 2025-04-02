package com.example.pixelprice.features.views.createProject.data.datasourse

import com.example.pixelprice.features.views.createProject.data.model.CreateProjectRequest
import com.example.pixelprice.features.views.createProject.data.model.ProjectDTO
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ProjectService {

    @POST("projects")
    suspend fun createProject(@Body request: CreateProjectRequest): Response<ProjectDTO>
}
