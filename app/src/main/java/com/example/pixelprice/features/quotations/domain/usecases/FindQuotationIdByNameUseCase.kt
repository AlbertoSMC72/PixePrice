package com.example.pixelprice.features.quotations.domain.usecases

import android.content.Context
import com.example.pixelprice.features.quotations.data.repository.QuotationRepository

class FindQuotationIdByNameUseCase(context: Context) {
    private val repository = QuotationRepository(context.applicationContext)

    suspend operator fun invoke(projectName: String): Result<Int> {
        if (projectName.isBlank()) {
            return Result.failure(IllegalArgumentException("El nombre del proyecto no puede estar vacío."))
        }
        return repository.findQuotationIdByName(projectName)
    }
}