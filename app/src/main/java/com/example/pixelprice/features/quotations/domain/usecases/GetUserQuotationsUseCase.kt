package com.example.pixelprice.features.quotations.domain.usecases

import android.content.Context
import com.example.pixelprice.features.quotations.data.remote.model.QuotationListItemDTO
import com.example.pixelprice.features.quotations.data.repository.QuotationRepository

class GetUserQuotationsUseCase(context: Context) {
    private val repository = QuotationRepository(context.applicationContext)

    suspend operator fun invoke(): Result<List<QuotationListItemDTO>> {
        return repository.getUserQuotations()
    }
}