package com.example.pixelprice.core.network

import android.content.Context
import android.net.Uri
import android.util.Log
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody

fun String.toTextRequestBody(): RequestBody =
    this.toRequestBody("text/plain".toMediaTypeOrNull())

fun Double.toTextRequestBody(): RequestBody =
    this.toString().toTextRequestBody()

fun Int.toTextRequestBody(): RequestBody =
    this.toString().toTextRequestBody()

fun Boolean.toTextRequestBody(): RequestBody =
    this.toString().toTextRequestBody()

fun Uri?.toImageMultipartBodyPart(
    context: Context,
    partName: String = "mockupImage" // Nombre del campo que espera la API
): MultipartBody.Part? {
    if (this == null) return null

    return try {
        val contentResolver = context.contentResolver
        val mimeType = contentResolver.getType(this) ?: "image/*" // Fallback a tipo genérico
        val extension = mimeType.substringAfterLast('/', "jpg") // Obtener extensión o fallback
        val fileName = "${partName}_${System.currentTimeMillis()}.$extension"

        Log.d("ApiServiceUtils", "Preparando imagen: Uri=$this, Mime=$mimeType, Name=$fileName")

        // Leer bytes desde el InputStream del Uri
        val inputStream = contentResolver.openInputStream(this)
        val imageBytes = inputStream?.readBytes()
        inputStream?.close() // ¡Importante cerrar el stream!

        if (imageBytes != null) {
            val imageRequestBody = imageBytes.toRequestBody(mimeType.toMediaTypeOrNull())
            MultipartBody.Part.createFormData(partName, fileName, imageRequestBody)
        } else {
            Log.e("ApiServiceUtils", "No se pudieron leer los bytes del Uri: $this")
            null
        }
    } catch (e: Exception) {
        Log.e("ApiServiceUtils", "Error convirtiendo Uri a MultipartBody.Part para $this", e)
        null // Devolver null si hay algún error
    }
}