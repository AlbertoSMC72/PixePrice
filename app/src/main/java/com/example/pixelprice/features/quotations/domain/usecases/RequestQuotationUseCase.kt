package com.example.pixelprice.features.quotations.domain.usecases

import android.content.Context
import android.net.Uri
import com.example.pixelprice.features.quotations.data.repository.QuotationRepository

class RequestQuotationUseCase(context: Context) {
    // El repositorio necesita contexto para procesar la imagen
    private val repository = QuotationRepository(context.applicationContext)

    suspend operator fun invoke(
        projectName: String,
        projectDescription: String,
        projectCapital: Double,
        projectIsSelfMade: Boolean,
        mockupImageUri: Uri?
    ): Result<Unit> {
        // Validaciones básicas
        if (projectName.isBlank() || projectDescription.isBlank() || projectCapital < 0) {
            return Result.failure(IllegalArgumentException("Datos del proyecto inválidos para cotización."))
        }
        // El repositorio se encarga de la lógica de red y conversión
        return repository.requestQuotation(
            projectName, projectDescription, projectCapital, projectIsSelfMade, mockupImageUri
        )
    }
}