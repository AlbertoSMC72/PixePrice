package com.example.pixelprice.features.quotations.domain.usecases

import android.content.Context
import com.example.pixelprice.features.quotations.data.repository.QuotationRepository

class DownloadQuotationDocxUseCase(context: Context) {
    private val repository = QuotationRepository(context.applicationContext)


    suspend operator fun invoke(quotationName: String): Result<Unit> {
        if (quotationName.isBlank()) {
            return Result.failure(IllegalArgumentException("Nombre de cotización inválido."))
        }
        return repository.downloadQuotationDocx(quotationName)
    }
}