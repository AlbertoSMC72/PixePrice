package com.example.pixelprice

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.example.pixelprice.core.navigation.NavigationWrapper

class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                // El usuario concedió el permiso de notificaciones
                Toast.makeText(this, "Permiso concedido", Toast.LENGTH_SHORT).show()
                Log.d("dbug", "Permiso de notificaciones concedido")
            } else {
                // El usuario denegó el permiso de notificaciones
                Toast.makeText(this, "Permiso denegado", Toast.LENGTH_SHORT).show()
                Log.d("dbug", "Permiso de notificaciones denegado")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permiso = Manifest.permission.POST_NOTIFICATIONS

            if (ContextCompat.checkSelfPermission(this, permiso) == PackageManager.PERMISSION_GRANTED) {
                Log.d("dbug", "Permiso de notificación ya concedido")
            } else {
                requestPermissionLauncher.launch(permiso)
            }
        }

        setContent {
            NavigationWrapper()
        }
    }
}
