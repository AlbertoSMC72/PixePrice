package com.example.pixelprice.features.views.home.data.datasourse

import com.example.pixelprice.features.views.home.data.model.ProjectDTO
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface ProjectService {
    @GET("users/{userId}/projects")
    suspend fun getProjectsByUser(@Path("userId") userId: Int): Response<List<ProjectDTO>>
}
