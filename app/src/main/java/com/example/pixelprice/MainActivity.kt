package com.example.pixelprice

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.pixelprice.core.navigation.NavigationWrapper
import com.example.pixelprice.core.service.FcmService
import com.example.pixelprice.features.projects.domain.usecases.FindProjectByNameUseCase
import com.example.pixelprice.features.projects.domain.usecases.UpdateProjectQuotationStatusUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch


object IntentHandler {
    private val _navIntentFlow = MutableSharedFlow<Intent>(extraBufferCapacity = 1)
    val navIntentFlow = _navIntentFlow.asSharedFlow()

    fun newIntent(intent: Intent) {
        _navIntentFlow.tryEmit(intent)
    }
}

class MainViewModel(application: android.app.Application) : AndroidViewModel(application) {
    private val findProjectByNameUseCase = FindProjectByNameUseCase()
    private val updateProjectQuotationStatusUseCase = UpdateProjectQuotationStatusUseCase()
    fun updateProjectStatusFromNotification(projectName: String?, quotationId: Int?) {
        if (projectName == null || quotationId == null || quotationId <= 0) {
            Log.w("MainViewModel", "Datos inválidos desde notificación: Name=$projectName, ID=$quotationId")
            return
        }
        viewModelScope.launch {
            Log.d("MainViewModel", "Intentando actualizar estado para proyecto '$projectName' con quoteId $quotationId")
            val findResult = findProjectByNameUseCase(projectName)
            findResult.onSuccess { projectEntity ->
                val updateResult = updateProjectQuotationStatusUseCase(
                    projectId = projectEntity.id,
                    quotationId = quotationId,
                    isPending = false
                )
                if (updateResult.isSuccess) {
                    Log.i("MainViewModel", "Estado del proyecto local '${projectEntity.name}' (ID: ${projectEntity.id}) actualizado por notificación.")
                } else {
                    Log.w("MainViewModel", "Fallo al actualizar estado del proyecto local '${projectEntity.name}' por notificación.")
                }
            }.onFailure { findError ->
                Log.e("MainViewModel", "No se encontró proyecto local con nombre '$projectName' para actualizar estado.", findError)
            }
        }
    }
}
class MainViewModelFactory(private val application: android.app.Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class for MainViewModelFactory")
    }
}


class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
        val message = if (isGranted) "Permiso concedido" else "Permiso denegado"
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        Log.d("MainActivity", "Permiso Notificaciones: $message")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        askNotificationPermission()
        intent?.let { IntentHandler.newIntent(it) }

        setContent {
            val navController = rememberNavController()
            val mainViewModel: MainViewModel = viewModel(factory = MainViewModelFactory(application))
            MainApp(navController = navController, mainViewModel = mainViewModel)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        IntentHandler.newIntent(intent)
        Log.d("MainActivity", "onNewIntent llamado.")
    }
    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permission = Manifest.permission.POST_NOTIFICATIONS
            when {
                ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED -> {
                    Log.d("MainActivity", "Permiso POST_NOTIFICATIONS ya concedido.")
                }
                shouldShowRequestPermissionRationale(permission) -> {
                    Log.d("MainActivity", "Mostrando explicación para permiso (opcional).")
                    requestPermissionLauncher.launch(permission)
                }
                else -> {
                    Log.d("MainActivity", "Solicitando permiso POST_NOTIFICATIONS.")
                    requestPermissionLauncher.launch(permission)
                }
            }
        } else {
            Log.d("MainActivity", "Permiso POST_NOTIFICATIONS no requerido en esta versión.")
        }
    }
}

@Composable
fun MainApp(navController: NavHostController, mainViewModel: MainViewModel) {
    var processedIntentHash by remember { mutableStateOf<Int?>(null) }
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            IntentHandler.navIntentFlow.collect { intent ->
                val intentHash = intent.hashCode()
                Log.d("MainAppComposable", "Intent recibido en colector: ${intent.action}, Hash: $intentHash")

                val isQuotationReady = intent.getBooleanExtra(FcmService.IS_QUOTATION_READY_KEY, false)
                val quotationId = intent.getIntExtra(FcmService.QUOTATION_ID_KEY, -1)
                val projectName = intent.getStringExtra(FcmService.PROJECT_NAME_KEY)

                if (isQuotationReady && quotationId != -1 && projectName != null && intentHash != processedIntentHash) {
                    Log.i("MainAppComposable", "Intent QUOTATION_READY detectado para '$projectName' (ID: $quotationId). Procesando...")
                    processedIntentHash = intentHash

                    Toast.makeText(context, "Cotización lista para '$projectName'", Toast.LENGTH_LONG).show()
                    mainViewModel.updateProjectStatusFromNotification(projectName, quotationId)

                } else {
                    if (intentHash == processedIntentHash) {
                        Log.d("MainAppComposable", "Intent ya procesado (hash: $intentHash).")
                    } else if (isQuotationReady){
                        Log.w("MainAppComposable", "Intent QUOTATION_READY con datos incompletos o inválidos.")
                    } else {
                        Log.d("MainAppComposable", "Intent no es de tipo QUOTATION_READY.")
                    }
                }
            }
        }
    }

    NavigationWrapper(navController = navController)
}