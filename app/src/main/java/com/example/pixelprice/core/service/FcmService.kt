package com.example.pixelprice.core.service

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.pixelprice.MainActivity
import com.example.pixelprice.MyApp
import com.example.pixelprice.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import java.util.concurrent.atomic.AtomicInteger

class FcmService : FirebaseMessagingService() {

    companion object {
        private val notificationIdGenerator = AtomicInteger(1000)
        const val IS_QUOTATION_READY_KEY = "is_quotation_ready"
        const val QUOTATION_ID_KEY = "quotation_id"
        const val PROJECT_NAME_KEY = "project_name"
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        Log.d("FCM_Service", "Mensaje FCM recibido!")
        Log.d("FCM_Service", "From: ${message.from}")
        message.notification?.let {
            Log.d("FCM_Service", "Notification Title: ${it.title}")
            Log.d("FCM_Service", "Notification Body: ${it.body}")
        }
        if (message.data.isNotEmpty()) {
            Log.d("FCM_Service", "Data Payload: ${message.data}")
        }

        handleNotificationData(applicationContext, message)
    }

    private fun handleNotificationData(context: Context, message: RemoteMessage) {
        val data = message.data
        val notificationType = data["type"]

        if (notificationType == "QUOTATION_READY") {
            val quotationIdStr = data["quotationId"]
            val quotationName = data["quotationName"]
            val backendQuotationId = data["backendQuotationId"]

            if (quotationName != null && backendQuotationId != null) {
                try {
                    val quotationIdInt = backendQuotationId.toInt()
                    Log.d("FCM_Service", "Cotización lista detectada. Proyecto: '$quotationName', ID Cotización: $quotationIdInt")

                    showQuotationReadyNotification(
                        context = context,
                        projectName = quotationName,
                        quotationId = quotationIdInt,
                        fcmNotification = message.notification
                    )

                } catch (e: NumberFormatException) {
                    Log.e("FCM_Service", "Error al parsear backendQuotationId '$backendQuotationId'", e)
                    showGenericNotification(context, message.notification)
                }
            } else {
                Log.w("FCM_Service", "Notificación QUOTATION_READY recibida sin quotationName o backendQuotationId.")
                showGenericNotification(context, message.notification)
            }
        } else {
            Log.d("FCM_Service", "Notificación genérica o tipo desconocido: $notificationType")
            showGenericNotification(context, message.notification)
        }
    }

    private fun showQuotationReadyNotification(
        context: Context,
        projectName: String,
        quotationId: Int,
        fcmNotification: RemoteMessage.Notification?
    ) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val uniqueNotificationId = notificationIdGenerator.incrementAndGet()

        val mainActivityIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra(IS_QUOTATION_READY_KEY, true)
            putExtra(PROJECT_NAME_KEY, projectName)
            putExtra(QUOTATION_ID_KEY, quotationId)
        }

        val pendingIntentFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            uniqueNotificationId,
            mainActivityIntent,
            pendingIntentFlag
        )

        val notificationBuilder = NotificationCompat.Builder(context, MyApp.NOTIFICATION_CHANNEL_ID)
            .setContentTitle(fcmNotification?.title ?: "¡Cotización Lista!")
            .setContentText(fcmNotification?.body ?: "La cotización para '$projectName' está lista.")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        notificationManager.notify(uniqueNotificationId, notificationBuilder.build())
        Log.d("FCM_Service", "Mostrando notificación QUOTATION_READY para '$projectName' (QuoteID: $quotationId), NotifID: $uniqueNotificationId")
    }

    private fun showGenericNotification(context: Context, fcmNotification: RemoteMessage.Notification?) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val uniqueNotificationId = notificationIdGenerator.incrementAndGet()

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntentFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_ONE_SHOT
        }
        val pendingIntent = PendingIntent.getActivity(context, uniqueNotificationId, openAppIntent, pendingIntentFlag)


        val notificationBuilder = NotificationCompat.Builder(context, MyApp.NOTIFICATION_CHANNEL_ID)
            .setContentTitle(fcmNotification?.title ?: "Nueva Notificación")
            .setContentText(fcmNotification?.body ?: "Tienes un nuevo mensaje.")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        notificationManager.notify(uniqueNotificationId, notificationBuilder.build())
        Log.d("FCM_Service", "Mostrando notificación genérica. NotifID: $uniqueNotificationId")
    }
}