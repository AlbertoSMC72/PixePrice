package com.example.pixelprice.core.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.pixelprice.core.local.appDatabase.DatabaseProvider
import com.example.pixelprice.core.local.chapters.entities.ChapterEntity
import com.example.watpato.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class DownloadService : Service() {

    private val CHANNEL_ID = "DOWNLOAD_SERVICE_CHANNEL"
    private val NOTIFICATION_ID = 123

    override fun onCreate() {
        super.onCreate()
        crearNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("Download_chapter", "Descargando libro")

        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Guardando capítulo...")
            .setContentText("Procesando en segundo plano")
            .setSmallIcon(R.drawable.watpato)
            .build()

        startForeground(NOTIFICATION_ID, notification)

        val chapterTitle = intent?.getStringExtra("CHAPTER_TITLE") ?: "Sin Título"
        val chapterContent = intent?.getStringExtra("CHAPTER_CONTENT") ?: "Sin Contenido"
        val chapterDate = intent?.getStringExtra("CHAPTER_DATE") ?: "N/A"


        CoroutineScope(Dispatchers.IO).launch {
            try {
                delay(5000)
                val chapterDAO = DatabaseProvider.getAppDataBase(applicationContext).chapterDAO()
                val chapterEntity = ChapterEntity(
                    title = chapterTitle,
                    content = chapterContent,
                    created_at = chapterDate
                )
                chapterDAO.saveChapter(chapterEntity)

            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                delay(10000)
                stopSelf()
            }
        }

        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun crearNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Canal de Descargas",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }
}
