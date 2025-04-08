    package com.example.pixelprice

    import android.app.Application
    import android.app.NotificationChannel
    import android.app.NotificationManager
    import android.content.Context
    import android.os.Build
    import android.util.Log
    import com.example.pixelprice.core.data.FirebaseTokenProvider
    import com.example.pixelprice.core.data.TokenManager
    import com.example.pixelprice.core.local.DatabaseProvider
    import com.example.pixelprice.core.network.RetrofitHelper
    import com.google.firebase.ktx.Firebase
    import com.google.firebase.messaging.ktx.messaging

    class MyApp : Application() {
        companion object {
            const val NOTIFICATION_CHANNEL_ID = "notification_fcm"
            private const val TAG = "MyAppLifecycle"
        }

        override fun onCreate() {
            super.onCreate()
            Log.i(TAG, "onCreate - Inicializando Core...")

            TokenManager.initialize(this)
            DatabaseProvider.initialize(this)
            RetrofitHelper.initialize(this)
            Log.i(TAG, "Core components inicializados.")

            createNotificationChannel()

            fetchFcmToken()

            Log.i(TAG, "onCreate completado.")
        }

        private fun fetchFcmToken() {
            Firebase.messaging.token.addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w("MyAppFCM", "Fetching FCM registration token failed", task.exception)
                    return@addOnCompleteListener
                }
                val token = task.result
                FirebaseTokenProvider.firebaseToken = token
                Log.d("MyAppFCM", "FCM Token obtenido (parcial): ${token?.take(10)}...")
            }
        }

        private fun createNotificationChannel() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channelName = "Notificaciones de Cotización"
                val channelDescription = "Notificaciones sobre el estado de las cotizaciones generadas."
                val importance = NotificationManager.IMPORTANCE_HIGH

                val channel = NotificationChannel(
                    NOTIFICATION_CHANNEL_ID,
                    channelName,
                    importance
                ).apply {
                    description = channelDescription
                }

                val notificationManager: NotificationManager? =
                    getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager

                if (notificationManager != null) {
                    notificationManager.createNotificationChannel(channel)
                    Log.i("MyApp", "Canal de notificación '$channelName' creado.")
                } else {
                    Log.e("MyApp", "No se pudo obtener NotificationManager para crear el canal.")
                }
            }
        }

    }