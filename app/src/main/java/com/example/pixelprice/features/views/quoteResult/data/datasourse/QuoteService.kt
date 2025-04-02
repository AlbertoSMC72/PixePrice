package com.example.pixelprice.features.views.quoteResult.data.datasourse

import com.example.pixelprice.features.views.quoteResult.data.model.QuoteDTO
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface QuoteService {
    @GET("projects/{id}/quote")
    suspend fun getQuote(@Path("id") projectId: Int): Response<QuoteDTO>
}
