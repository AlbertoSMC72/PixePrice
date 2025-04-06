package com.example.pixelprice.core.service

import android.app.NotificationManager
import android.app.PendingIntent // *** NUEVO ***
import android.content.Context // *** NUEVO ***
import android.content.Intent // *** NUEVO ***
import android.os.Build // *** NUEVO ***
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.pixelprice.MainActivity // *** NUEVO: Para lanzar la app ***
import com.example.pixelprice.MyApp
import com.example.pixelprice.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import java.util.concurrent.atomic.AtomicInteger // *** NUEVO: Para IDs únicos de notificación ***

class FcmService : FirebaseMessagingService() {

    companion object {
        private val notificationIdGenerator = AtomicInteger(1000) // IDs únicos
        // Claves para los datos extra en el Intent que abre MainActivity
        const val IS_QUOTATION_READY_KEY = "is_quotation_ready" // Booleano para identificar el tipo
        const val QUOTATION_ID_KEY = "quotation_id"           // ID de la cotización generada
        const val PROJECT_NAME_KEY = "project_name"           // Nombre del proyecto asociado
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

        // Analizar los datos y decidir qué notificación mostrar
        handleNotificationData(applicationContext, message)
    }

    private fun handleNotificationData(context: Context, message: RemoteMessage) {
        val data = message.data
        val notificationType = data["type"] // Tipo personalizado desde el backend

        if (notificationType == "QUOTATION_READY") {
            val quotationIdStr = data["quotationId"]
            val quotationName = data["quotationName"] // Nombre del proyecto
            val backendQuotationId = data["backendQuotationId"] // ID real de la cotización

            if (quotationName != null && backendQuotationId != null) {
                try {
                    val quotationIdInt = backendQuotationId.toInt()
                    Log.d("FCM_Service", "Cotización lista detectada. Proyecto: '$quotationName', ID Cotización: $quotationIdInt")

                    showQuotationReadyNotification(
                        context = context,
                        projectName = quotationName,
                        quotationId = quotationIdInt, // Pasar el ID real
                        fcmNotification = message.notification // Pasar título/cuerpo de FCM
                    )

                } catch (e: NumberFormatException) {
                    Log.e("FCM_Service", "Error al parsear backendQuotationId '$backendQuotationId'", e)
                    showGenericNotification(context, message.notification) // Fallback a genérica
                }
            } else {
                Log.w("FCM_Service", "Notificación QUOTATION_READY recibida sin quotationName o backendQuotationId.")
                showGenericNotification(context, message.notification) // Fallback
            }
        } else {
            Log.d("FCM_Service", "Notificación genérica o tipo desconocido: $notificationType")
            showGenericNotification(context, message.notification) // Fallback
        }
    }

    /**
     * Muestra notificación de cotización lista. Al tocarla, abre MainActivity,
     * la cual detectará los extras y actualizará la DB local.
     */
    private fun showQuotationReadyNotification(
        context: Context,
        projectName: String,
        quotationId: Int, // ID de la cotización generada
        fcmNotification: RemoteMessage.Notification?
    ) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val uniqueNotificationId = notificationIdGenerator.incrementAndGet()

        // --- Intent para abrir MainActivity (sin navegación específica) ---
        val mainActivityIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK // Inicia o trae al frente
            // Añadir extras para que MainApp/MainViewModel los procesen
            putExtra(IS_QUOTATION_READY_KEY, true)
            putExtra(PROJECT_NAME_KEY, projectName)
            putExtra(QUOTATION_ID_KEY, quotationId) // ID de la cotización
        }

        // --- PendingIntent ---
        val pendingIntentFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            uniqueNotificationId, // Request code único
            mainActivityIntent,
            pendingIntentFlag
        )

        // --- Construir Notificación ---
        val notificationBuilder = NotificationCompat.Builder(context, MyApp.NOTIFICATION_CHANNEL_ID)
            .setContentTitle(fcmNotification?.title ?: "¡Cotización Lista!")
            .setContentText(fcmNotification?.body ?: "La cotización para '$projectName' está lista.")
            .setSmallIcon(R.drawable.ic_launcher_foreground) // **REEMPLAZA con tu ícono**
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true) // Se cierra al tocarla
            .setContentIntent(pendingIntent) // Acción al tocar

        // --- Mostrar ---
        notificationManager.notify(uniqueNotificationId, notificationBuilder.build())
        Log.d("FCM_Service", "Mostrando notificación QUOTATION_READY para '$projectName' (QuoteID: $quotationId), NotifID: $uniqueNotificationId")
    }

    /**
     * Muestra una notificación genérica (sin acción específica al tocar).
     */
    private fun showGenericNotification(context: Context, fcmNotification: RemoteMessage.Notification?) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val uniqueNotificationId = notificationIdGenerator.incrementAndGet()

        // Intent genérico para solo abrir la app
        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntentFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE // One shot para intento simple
        } else {
            PendingIntent.FLAG_ONE_SHOT
        }
        val pendingIntent = PendingIntent.getActivity(context, uniqueNotificationId, openAppIntent, pendingIntentFlag)


        val notificationBuilder = NotificationCompat.Builder(context, MyApp.NOTIFICATION_CHANNEL_ID)
            .setContentTitle(fcmNotification?.title ?: "Nueva Notificación")
            .setContentText(fcmNotification?.body ?: "Tienes un nuevo mensaje.")
            .setSmallIcon(R.drawable.ic_launcher_foreground) // **REEMPLAZA con tu ícono**
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent) // Abrir app al tocar

        notificationManager.notify(uniqueNotificationId, notificationBuilder.build())
        Log.d("FCM_Service", "Mostrando notificación genérica. NotifID: $uniqueNotificationId")
    }
}