package com.example.pixelprice.features.quotations.data.datasource

import com.example.pixelprice.features.quotations.data.remote.model.QuotationIdDTO
import com.example.pixelprice.features.quotations.data.remote.model.QuotationListItemDTO
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface QuotationService {
    @Multipart
    @POST("/quotations")
    suspend fun createQuotation(
        @Part("name") name: RequestBody,
        @Part("description") description: RequestBody,
        @Part("capital") capital: RequestBody,
        @Part("isSelfMade") isSelfMade: RequestBody,
        @Part mockupImage: MultipartBody.Part?
    ): Response<Unit>

    @GET("/quotations/user")
    suspend fun getUserQuotations(): Response<List<QuotationListItemDTO>>

    @GET("/quotations/project/{projectName}")
    suspend fun getQuotationIdByName(@Path("projectName") projectName: String): Response<QuotationIdDTO>

    @GET("/quotations/{name}/download")
    suspend fun downloadQuotationDocx(@Path("name") quotationName: String): Response<ResponseBody>
}