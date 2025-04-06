    // --- MODIFICADO: pixelprice/MyApp.kt ---
    package com.example.pixelprice

    import android.app.Application
    import android.app.NotificationChannel
    import android.app.NotificationManager
    import android.content.Context // Necesario para el casting seguro
    import android.os.Build
    import android.util.Log
    import com.example.pixelprice.core.data.FirebaseTokenProvider
    import com.example.pixelprice.core.data.TokenManager      // Importar
    import com.example.pixelprice.core.local.DatabaseProvider
    import com.example.pixelprice.core.network.RetrofitHelper // Importar
    import com.google.firebase.ktx.Firebase
    import com.google.firebase.messaging.ktx.messaging

    class MyApp : Application() {
        companion object {
            const val NOTIFICATION_CHANNEL_ID = "notification_fcm"
            private const val TAG = "MyAppLifecycle" // Tag para logs
        }

        override fun onCreate() {
            super.onCreate()
            Log.i(TAG, "onCreate - Inicializando Core...")

            // --- Inicializar Componentes Core ---
            // Se inicializan aquí para que estén disponibles en toda la app sin pasar Context
            TokenManager.initialize(this)
            DatabaseProvider.initialize(this)
            RetrofitHelper.initialize(this)
            Log.i(TAG, "Core components inicializados.")
            // ------------------------------------

            createNotificationChannel()

            // Obtener token FCM
            fetchFcmToken()

            Log.i(TAG, "onCreate completado.")
        }

        private fun fetchFcmToken() {
            Firebase.messaging.token.addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w("MyAppFCM", "Fetching FCM registration token failed", task.exception)
                    // Podrías querer reintentar o manejar este error de alguna forma
                    return@addOnCompleteListener
                }
                // Get new FCM registration token
                val token = task.result
                FirebaseTokenProvider.firebaseToken = token
                // Loguear solo una parte del token por seguridad/claridad
                Log.d("MyAppFCM", "FCM Token obtenido (parcial): ${token?.take(10)}...")
            }
        }

        private fun createNotificationChannel() {
            // Solo necesario en Android Oreo (API 26) o superior
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // Usar constantes para nombre y descripción si se repiten
                val channelName = "Notificaciones de Cotización"
                val channelDescription = "Notificaciones sobre el estado de las cotizaciones generadas."
                val importance = NotificationManager.IMPORTANCE_HIGH // Alta importancia para notificaciones clave

                val channel = NotificationChannel(
                    NOTIFICATION_CHANNEL_ID,
                    channelName,
                    importance
                ).apply { // Usar apply para configurar el canal limpiamente
                    description = channelDescription
                    // Opcional: Configurar luces, vibración por defecto para este canal
                    // enableLights(true)
                    // lightColor = Color.CYAN // O un color de tu tema
                    // enableVibration(true)
                    // vibrationPattern = longArrayOf(100, 200, 300, 400, 500) // Ejemplo
                }

                // Obtener el servicio NotificationManager de forma segura
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

        // Opcional: Manejar la terminación si necesitas limpiar algo (ej. cerrar DB explícitamente)
        // override fun onTerminate() {
        //     super.onTerminate()
        //     Log.i(TAG, "onTerminate - Limpiando recursos...")
        //     DatabaseProvider.destroyDataBase() // Ejemplo
        // }
    }