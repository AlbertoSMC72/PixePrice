package com.example.pixelprice.features.views.quoteResult.domain

import com.example.pixelprice.features.views.quoteResult.data.model.QuoteDTO
import com.example.pixelprice.features.views.quoteResult.data.repository.QuoteRepository

class GetQuoteUseCase {
    private val repository = QuoteRepository()

    suspend operator fun invoke(projectId: Int): Result<QuoteDTO> {
        return repository.getQuote(projectId)
    }
}
