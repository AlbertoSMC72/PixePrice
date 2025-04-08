package com.example.pixelprice.features.quotations.domain.usecases

import android.content.Context
import android.net.Uri
import com.example.pixelprice.features.quotations.data.repository.QuotationRepository

class RequestQuotationUseCase(context: Context) {
    private val repository = QuotationRepository(context.applicationContext)

    suspend operator fun invoke(
        projectName: String,
        projectDescription: String,
        projectCapital: Double,
        projectIsSelfMade: Boolean,
        mockupImageUri: Uri?
    ): Result<Unit> {
        if (projectName.isBlank() || projectDescription.isBlank() || projectCapital < 0) {
            return Result.failure(IllegalArgumentException("Datos del proyecto inválidos para cotización."))
        }
        return repository.requestQuotation(
            projectName, projectDescription, projectCapital, projectIsSelfMade, mockupImageUri
        )
    }
}