package com.example.pixelprice.core.utils

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import com.example.pixelprice.BuildConfig // Ahora debería resolverse tras habilitar buildConfig
import java.io.File
import java.io.IOException

object ComposeFileProvider {

    private const val IMAGE_SUBDIR = "images"
    // Accede a BuildConfig de forma segura
    private val AUTHORITY: String? by lazy {
        try {
            "${BuildConfig.APPLICATION_ID}.provider"
        } catch (e: NoClassDefFoundError){
            Log.e("ComposeFileProvider", "BuildConfig no encontrado. Asegúrate que 'buildConfig = true' esté en build.gradle(.kts) y el proyecto esté sincronizado/reconstruido.")
            null
        }

    }

    fun getImageUri(context: Context): Uri? {
        val authority = AUTHORITY ?: return null // Salir si no se pudo obtener la autoridad

        return try {
            val imagePath = File(context.cacheDir, IMAGE_SUBDIR)
            if (!imagePath.exists()) {
                if (!imagePath.mkdirs()) {
                    Log.e("ComposeFileProvider", "No se pudo crear el directorio: ${imagePath.absolutePath}")
                    return null
                }
            }
            Log.d("ComposeFileProvider", "Directorio de caché de imágenes: ${imagePath.absolutePath}")

            val tempFile = File.createTempFile(
                "temp_image_${System.currentTimeMillis()}_",
                ".jpg",
                imagePath
            )
            Log.d("ComposeFileProvider", "Archivo temporal creado: ${tempFile.absolutePath}")

            FileProvider.getUriForFile(
                context,
                authority, // Usar la autoridad obtenida
                tempFile
            )
        } catch (e: IOException) {
            Log.e("ComposeFileProvider", "Error creando archivo temporal", e)
            null
        } catch (e: IllegalArgumentException) {
            Log.e("ComposeFileProvider", "Error generando Uri con FileProvider. Verifica la autoridad ('$authority') y la configuración del Manifest.", e)
            null
        } catch (e: Exception) {
            Log.e("ComposeFileProvider", "Error inesperado en getImageUri", e)
            null
        }
    }
}