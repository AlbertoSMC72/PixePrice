package com.example.pixelprice.features.views.projectDetail.data.datasourse

import com.example.pixelprice.features.views.createProject.data.model.ProjectDTO
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ProjectService {

    @GET("projects/{id}")
    suspend fun getProjectById(@Path("id") id: Int): Response<ProjectDTO>

    @POST("projects/{id}/quote")
    suspend fun requestQuote(@Path("id") id: Int): Response<Unit>
}
