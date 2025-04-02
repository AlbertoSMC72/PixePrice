package com.example.pixelprice.core.service

import android.app.NotificationManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.pixelprice.MyApp
import com.example.watpato.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class FcmService : FirebaseMessagingService() {
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Log.d("FCM", message.notification?.body.toString())
        showNotification(message)
    }

    private fun showNotification(message: RemoteMessage) {
        val notificationManager = getSystemService(NotificationManager::class.java)
        val notification = NotificationCompat.Builder(this, MyApp.NOTIFICATION_CHANNEL_ID)
            .setContentTitle(message.notification?.title)
            .setContentText(message.notification?.body)
            .setSmallIcon(R.drawable.watpato)
            .setAutoCancel(true)
            .build()
        notificationManager.notify(1, notification)
    }
}