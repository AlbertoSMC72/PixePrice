package com.example.pixelprice.features.quotations.data.repository

import android.app.DownloadManager
import android.content.ContentValues
import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.system.Os.remove
import android.util.Log
import android.widget.Toast
import androidx.documentfile.provider.DocumentFile
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

    companion object {
        private const val PREFS_NAME_PROFILE = "pixelprice_profile_prefs"
        private const val KEY_DOWNLOAD_DIR_URI = "download_directory_uri"
    }
    private val profilePrefs: SharedPreferences by lazy {
        applicationContext.getSharedPreferences(PREFS_NAME_PROFILE, Context.MODE_PRIVATE)
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
        return try {
            Log.d("QuotationRepository", "Iniciando descarga DOCX para Nombre: $quotationName")
            val response = quotationService.downloadQuotationDocx(quotationName)

            if (response.isSuccessful && response.body() != null) {
                val directoryUriString = profilePrefs.getString(KEY_DOWNLOAD_DIR_URI, null)
                val targetDirectoryUri = directoryUriString?.let {
                    try { Uri.parse(it) } catch (e: Exception) { null }
                }
                Log.d("QuotationRepository", "Directorio de destino leído de prefs: $targetDirectoryUri")

                // Llamar a saveResponseBodyToFile pasando el URI de destino
                saveResponseBodyToFile(response.body()!!, quotationName, targetDirectoryUri)
                Result.success(Unit)
            } else {
                handleApiError(response.code(), response.errorBody()?.string(), "Descargar DOCX")
            }
        } catch (e: IOException) {
            Log.e("QuotationRepository", "Error de red/IO descargando DOCX: $quotationName", e)
            Result.failure(QuotationException.DownloadFailed("Error de red/IO durante la descarga.", e))
        } catch (e: SecurityException) {
            Log.e("QuotationRepository", "Error de permisos descargando DOCX: $quotationName", e)
            Result.failure(QuotationException.DownloadFailed("Permiso denegado para guardar archivo.", e))
        } catch (e: Exception) {
            Log.e("QuotationRepository", "Error inesperado descargando DOCX: $quotationName", e)
            Result.failure(QuotationException.DownloadFailed("Error inesperado durante la descarga: ${e.message}", e))
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
    private suspend fun saveResponseBodyToFile(body: ResponseBody, name: String, targetDirectoryUri: Uri?) {
        withContext(Dispatchers.IO) {
            val cleanName = name.replace(Regex("[^a-zA-Z0-9_.-]"), "_")
            val fileName = "Cotizacion_${cleanName}_${System.currentTimeMillis()}.docx"
            val mimeType = "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
            var outputStream: OutputStream? = null
            var inputStream: InputStream? = null
            var savedToSelectedFolder = false // Flag para saber dónde se guardó
            var targetDirName: String? = null // Nombre de la carpeta seleccionada

            try {
                // Intentar usar la carpeta seleccionada por SAF si existe y es válida
                if (targetDirectoryUri != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    Log.d("SaveToFile", "Intentando usar directorio SAF: $targetDirectoryUri")
                    val directory = DocumentFile.fromTreeUri(applicationContext, targetDirectoryUri)

                    if (directory != null) {
                        targetDirName = directory.name // Obtener nombre para el Toast
                        Log.d("SaveToFile", "DocumentFile obtenido. Nombre: $targetDirName, Es Directorio: ${directory.isDirectory}, Puede Escribir: ${directory.canWrite()}")

                        if (directory.isDirectory && directory.canWrite()) {
                            val newFile = directory.createFile(mimeType, fileName)
                            if (newFile != null) {
                                Log.d("SaveToFile", "Archivo creado en SAF: ${newFile.uri}")
                                outputStream = applicationContext.contentResolver.openOutputStream(newFile.uri)
                                if (outputStream != null) {
                                    Log.i("SaveToFile", "Guardando en directorio SAF seleccionado: ${newFile.uri}")
                                    savedToSelectedFolder = true // Marcamos éxito con SAF
                                } else {
                                    Log.e("SaveToFile", "Error: openOutputStream devolvió null para ${newFile.uri}")
                                }
                            } else {
                                Log.e("SaveToFile", "Error: directory.createFile devolvió null en $targetDirectoryUri")
                            }
                        } else {
                            Log.w("SaveToFile", "Directorio SAF no es válido o no se puede escribir. Nombre: $targetDirName")
                            // Podríamos limpiar la preferencia aquí si falla canWrite persistentemente
                            // profilePrefs.edit { remove(KEY_DOWNLOAD_DIR_URI) }
                        }
                    } else {
                        Log.e("SaveToFile", "Error: DocumentFile.fromTreeUri devolvió null para $targetDirectoryUri")
                        // Limpiar preferencia si el URI es inválido
                        profilePrefs.edit().remove(KEY_DOWNLOAD_DIR_URI).apply()
                    }
                } else {
                    Log.d("SaveToFile", "No hay directorio SAF seleccionado o versión Android < Lollipop.")
                }

                // Si no se pudo guardar con SAF (outputStream sigue null), usar fallback
                if (outputStream == null) {
                    Log.w("SaveToFile", "Fallback: Guardando en Descargas.")
                    savedToSelectedFolder = false // Asegurar que el flag es false
                    targetDirName = null
                    outputStream = saveToDownloadsFallback(fileName, mimeType)
                }

                // Si AÚN no se pudo obtener un OutputStream (ni con SAF ni con fallback)
                if (outputStream == null) {
                    throw IOException("No se pudo obtener un flujo de salida válido para guardar el archivo.")
                }

                // --- Escritura del archivo ---
                inputStream = body.byteStream()
                Log.d("SaveToFile", "Iniciando copia de bytes...")
                val copied = inputStream.copyTo(outputStream)
                Log.i("SaveToFile", "Archivo guardado. Bytes copiados: $copied. En carpeta seleccionada: $savedToSelectedFolder")

                // --- Mensaje Toast Mejorado ---
                val finalMessage = if (savedToSelectedFolder && targetDirName != null) {
                    "Guardado en '$targetDirName': $fileName"
                } else {
                    "Guardado en Descargas: $fileName" // Mensaje fallback
                }

                withContext(Dispatchers.Main) {
                    Toast.makeText(applicationContext, finalMessage, Toast.LENGTH_LONG).show()
                }

            } catch (e: Exception) {
                Log.e("SaveToFile", "Error durante el guardado del archivo DOCX ($fileName)", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(applicationContext, "Error al guardar descarga: ${e.message}", Toast.LENGTH_SHORT).show()
                }
                // Relanzar para que el UseCase/ViewModel lo capture como fallo
                throw IOException("Error al guardar el archivo: ${e.message}", e)
            } finally {
                try { inputStream?.close() } catch (e: IOException) { Log.e("SaveToFile", "Error cerrando InputStream", e) }
                try { outputStream?.close() } catch (e: IOException) { Log.e("SaveToFile", "Error cerrando OutputStream", e) }
            }
        }
    }

    @Throws(IOException::class)
    private fun saveToDownloadsFallback(fileName: String, mimeType: String): OutputStream? {
        // ... (código existente sin cambios) ...
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // API 29+ (Android 10+) - MediaStore
            val resolver = applicationContext.contentResolver
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }
            val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                ?: throw IOException("Error al crear entrada en MediaStore (fallback) para $fileName")
            return resolver.openOutputStream(uri)
        } else {
            // API < 29 (Android < 10) - Acceso directo
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (!downloadsDir.exists() && !downloadsDir.mkdirs()) {
                throw IOException("No se pudo crear el directorio de Descargas (fallback).")
            }
            val file = File(downloadsDir, fileName)
            return FileOutputStream(file)
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