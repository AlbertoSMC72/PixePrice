package com.example.pixelprice

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.util.Log
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging

class MyApp : Application() {
    companion object {
        const val NOTIFICATION_CHANNEL_ID = "notification_fcm"
    }

    override fun onCreate() {
        super.onCreate()
        println("App creada")

        createNotificationChannel()

        Firebase.messaging.token.addOnCompleteListener {
            if (!it.isSuccessful) {
                Log.d("dbug", "Token no fue generado")
                println("El token no fue generado correctamente")
                return@addOnCompleteListener
            }
            val token = it.result
            com.example.pixelprice.core.data.FirebaseTokenProvider.firebaseToken = token
            Log.d("dbug", token)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "Notificacion de fcm",
                NotificationManager.IMPORTANCE_HIGH
            )
            channel.description = "Esta notificacion va ser recibida desde fcm"
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
}