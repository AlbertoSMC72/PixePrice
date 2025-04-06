package com.example.pixelprice.features.quotations.data.repository

import android.app.DownloadManager
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import com.example.pixelprice.core.data.UserInfoProvider
import com.example.pixelprice.features.quotations.data.datasource.QuotationService
import com.example.pixelprice.core.network.RetrofitHelper
import com.example.pixelprice.core.network.toTextRequestBody
import com.example.pixelprice.core.network.toImageMultipartBodyPart
import com.example.pixelprice.features.quotations.data.remote.model.QuotationListItemDTO
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

class QuotationRepository(
    // Necesita contexto para procesar imagen y usar DownloadManager
    private val applicationContext: Context
) {

    private val quotationService: QuotationService by lazy {
        RetrofitHelper.createService(QuotationService::class.java)
    }

    // --- Solicitar Creación de Cotización ---
    suspend fun requestQuotation(
        projectName: String,
        projectDescription: String,
        projectCapital: Double,
        projectIsSelfMade: Boolean,
        mockupImageUri: Uri? // URI local de la imagen
    ): Result<Unit> {
        val userId = UserInfoProvider.userID
        if (userId == 0) return Result.failure(QuotationException.NotAuthenticated())

        Log.d("QuotationRepository", "Preparando solicitud de cotización para: $projectName")

        return try {
            // Convertir datos a RequestBody y MultipartBody.Part
            val namePart = projectName.toTextRequestBody()
            val descriptionPart = projectDescription.toTextRequestBody()
            val capitalPart = projectCapital.toTextRequestBody()
            val isSelfMadePart = projectIsSelfMade.toTextRequestBody()
            val imagePart = mockupImageUri.toImageMultipartBodyPart(applicationContext) // Usa helper

            Log.d("QuotationRepository", "Imagen incluida en request: ${imagePart != null}")

            val response = quotationService.createQuotation(
                name = namePart,
                description = descriptionPart,
                capital = capitalPart,
                isSelfMade = isSelfMadePart,
                mockupImage = imagePart
            )

            if (response.isSuccessful && response.code() == 202) { // 202 Accepted
                Log.i("QuotationRepository", "Solicitud de cotización aceptada por API para: $projectName")
                Result.success(Unit)
            } else {
                handleApiError(response.code(), response.errorBody()?.string(), "Solicitar cotización")
            }
        } catch (e: IOException) {
            Log.e("QuotationRepository", "Error de red solicitando cotización", e)
            Result.failure(QuotationException.NetworkError(e))
        } catch (e: Exception) {
            Log.e("QuotationRepository", "Error inesperado solicitando cotización", e)
            Result.failure(QuotationException.UnknownError("Error inesperado: ${e.message}", e))
        }
    }

    // --- Obtener Lista de Cotizaciones del Usuario ---
    suspend fun getUserQuotations(): Result<List<QuotationListItemDTO>> {
        return try {
            Log.d("QuotationRepository", "Obteniendo cotizaciones para usuario ID: ${UserInfoProvider.userID}")
            val response = quotationService.getUserQuotations()
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                handleApiError(response.code(), response.errorBody()?.string(), "Obtener lista")
            }
        } catch (e: IOException) {
            Log.e("QuotationRepository", "Error de red obteniendo lista de cotizaciones", e)
            Result.failure(QuotationException.NetworkError(e))
        } catch (e: Exception) {
            Log.e("QuotationRepository", "Error inesperado obteniendo lista de cotizaciones", e)
            Result.failure(QuotationException.UnknownError("Error inesperado: ${e.message}", e))
        }
    }

    // --- Encontrar ID de Cotización por Nombre ---
    suspend fun findQuotationIdByName(projectName: String): Result<Int> {
        return try {
            Log.d("QuotationRepository", "Buscando cotización por nombre: $projectName")
            val response = quotationService.getQuotationIdByName(projectName)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    Result.success(body.id)
                } else {
                    Log.w("QuotationRepository", "API OK pero cuerpo nulo buscando por nombre: $projectName")
                    // Considerar si 404 debería ser un error o un estado específico
                    Result.failure(QuotationException.NotFound("No se encontró cotización para '$projectName' (cuerpo nulo)."))
                }
            } else if (response.code() == 404) {
                Log.i("QuotationRepository", "Cotización no encontrada por nombre: $projectName (404)")
                Result.failure(QuotationException.NotFound("No se encontró cotización para '$projectName'."))
            } else {
                handleApiError(response.code(), response.errorBody()?.string(), "Buscar por nombre")
            }
        } catch (e: IOException) {
            Log.e("QuotationRepository", "Error de red buscando cotización por nombre", e)
            Result.failure(QuotationException.NetworkError(e))
        } catch (e: Exception) {
            Log.e("QuotationRepository", "Error inesperado buscando cotización por nombre", e)
            Result.failure(QuotationException.UnknownError("Error inesperado: ${e.message}", e))
        }
    }

    suspend fun downloadQuotationDocx(quotationName: String): Result<Unit> {
        // La comprobación de permiso WRITE_EXTERNAL_STORAGE se hace en ViewModel
        return try {
            Log.d("QuotationRepository", "Iniciando descarga DOCX para Nombre: $quotationName")
            // *** LLAMAR AL SERVICIO RETROFIT ***
            val response = quotationService.downloadQuotationDocx(quotationName)

            if (response.isSuccessful && response.body() != null) {
                // *** LLAMAR AL HELPER DE ESCRITURA MANUAL ***
                saveResponseBodyToFile(response.body()!!, quotationName) // Pasar nombre
                Result.success(Unit)
            } else {
                // Manejar error de API (404, 500, etc.)
                handleApiError(response.code(), response.errorBody()?.string(), "Descargar DOCX")
            }
        } catch (e: IOException) {
            // Error de Red/IO durante la petición o escritura
            Log.e("QuotationRepository", "Error de red/IO descargando DOCX: $quotationName", e)
            Result.failure(QuotationException.DownloadFailed("Error de red/IO durante la descarga.", e))
        } catch (e: SecurityException) {
            // Error de permiso al intentar escribir (si falla la comprobación previa)
            Log.e("QuotationRepository", "Error de permisos descargando DOCX: $quotationName", e)
            Result.failure(QuotationException.DownloadFailed("Permiso denegado para guardar archivo.", e))
        } catch (e: Exception) {
            // Otros errores inesperados
            Log.e("QuotationRepository", "Error inesperado descargando DOCX: $quotationName", e)
            Result.failure(QuotationException.DownloadFailed("Error inesperado durante la descarga.", e))
        }
    }


    // --- Helpers Internos ---

    // Maneja errores comunes de API
    private fun <T> handleApiError(code: Int, errorBody: String?, context: String = "API"): Result<T> {
        val errorMsg = errorBody ?: "Sin detalles"
        Log.w("QuotationRepository", "Error $context ($code): $errorMsg")
        // Mapear errores específicos
        return Result.failure(
            when (code) {
                401 -> QuotationException.NotAuthenticated("No autorizado (401). Verifica el token.")
                403 -> QuotationException.Forbidden("Acceso prohibido (403).")
                404 -> QuotationException.NotFound("Recurso no encontrado (404).")
                409 -> QuotationException.Conflict("Conflicto (409): $errorMsg") // Ej: Cotización ya existe
                422 -> QuotationException.ValidationError("Datos inválidos (422): $errorMsg")
                in 500..599 -> QuotationException.ServerError("Error del servidor ($code): $errorMsg")
                else -> QuotationException.UnknownError("Error $context ($code): $errorMsg")
            }
        )
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    private suspend fun saveResponseBodyToFile(body: ResponseBody, name: String) {
        withContext(Dispatchers.IO) {
            val cleanName = name.replace(Regex("[^a-zA-Z0-9_.-]"), "_")
            // Usar timestamp para nombre único si no tenemos ID explícito aquí
            val fileName = "Cotizacion_${cleanName}_${System.currentTimeMillis()}.docx"
            val mimeType = "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
            var outputStream: OutputStream? = null
            var inputStream: InputStream? = null

            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    // --- API 29+ (Android 10+) ---
                    val resolver = applicationContext.contentResolver
                    val contentValues = ContentValues().apply {
                        put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                        put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                        put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                    }
                    val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                        ?: throw IOException("Error al crear entrada en MediaStore para $fileName")
                    outputStream = resolver.openOutputStream(uri)
                        ?: throw IOException("Error al abrir OutputStream para MediaStore Uri: $uri")

                } else {
                    // --- API < 29 (Android < 10) ---
                    val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                    if (!downloadsDir.exists()) { downloadsDir.mkdirs() }
                    val file = File(downloadsDir, fileName)
                    outputStream = FileOutputStream(file)
                }

                // Escribir los bytes
                inputStream = body.byteStream()
                val copied = inputStream.copyTo(outputStream)
                Log.i("QuotationRepository", "Archivo DOCX guardado en Descargas: $fileName, Bytes: $copied")

                // Mostrar Toast de éxito
                withContext(Dispatchers.Main) {
                    Toast.makeText(applicationContext, "Descarga completada: $fileName", Toast.LENGTH_LONG).show()
                }

            } catch (e: Exception) { // Capturar cualquier excepción durante la escritura
                Log.e("QuotationRepository", "Error al guardar archivo DOCX descargado ($fileName)", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(applicationContext, "Error al guardar descarga", Toast.LENGTH_SHORT).show()
                }
                // Relanzar para que el UseCase/ViewModel lo capture como fallo
                throw IOException("Error al guardar el archivo: ${e.message}", e)
            } finally {
                try { inputStream?.close() } catch (e: IOException) { /* ignore */ }
                try { outputStream?.close() } catch (e: IOException) { /* ignore */ }
            }
        }
    }
}


// Excepciones específicas para Cotizaciones
sealed class QuotationException(message: String, cause: Throwable? = null) : Exception(message, cause) {
    class NetworkError(cause: Throwable, message: String = "Error de red. Verifica tu conexión.") : QuotationException(message, cause)
    class UnknownError(message: String, cause: Throwable? = null) : QuotationException(message, cause)
    class NotFound(message: String) : QuotationException(message)
    class NotAuthenticated(message: String = "Usuario no autenticado.") : QuotationException(message)
    class Forbidden(message: String) : QuotationException(message)
    class Conflict(message: String) : QuotationException(message)
    class ValidationError(message: String) : QuotationException(message)
    class ServerError(message: String) : QuotationException(message)
    class DownloadFailed(message: String, cause: Throwable? = null) : QuotationException(message, cause)
}