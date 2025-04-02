package com.example.pixelprice.features.views.quoteResult.data.repository

import com.example.pixelprice.core.network.RetrofitHelper
import com.example.pixelprice.features.views.quoteResult.data.datasourse.QuoteService
import com.example.pixelprice.features.views.quoteResult.data.model.QuoteDTO

class QuoteRepository {
    private val service = RetrofitHelper.createService(QuoteService::class.java)

    suspend fun getQuote(projectId: Int): Result<QuoteDTO> = try {
        val response = service.getQuote(projectId)
        if (response.isSuccessful) Result.success(response.body()!!)
        else Result.failure(Exception("Error ${response.code()}"))
    } catch (e: Exception) {
        Result.failure(e)
    }
}
