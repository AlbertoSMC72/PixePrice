package com.example.pixelprice.features.projects.presentation.detail

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.ImageNotSupported
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.pixelprice.ui.theme.*
import kotlinx.coroutines.flow.collectLatest
import android.Manifest
import android.net.Uri
import android.os.Build // Necesario para comprobación de versión
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.ImageNotSupported
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.pixelprice.core.utils.ComposeFileProvider
import com.example.pixelprice.ui.theme.*
import kotlinx.coroutines.flow.collectLatest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Helper para formatear timestamp (Completo)
fun formatTimestamp(timestamp: Long): String {
    return try {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        sdf.format(Date(timestamp))
    } catch (e: Exception) {
        "Fecha inválida"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectDetailScreen(
    viewModel: ProjectDetailViewModel,
    projectId: Int,
    onNavigateToProcessing: (Int) -> Unit,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(projectId) {
        viewModel.loadProject(projectId)
    }

    var tempCameraImageUri by remember { mutableStateOf<Uri?>(null) }

    // --- Launchers de Permisos y Actividades ---

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { viewModel.onImageSelected(it) }
    }
    val galleryPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            galleryLauncher.launch("image/*")
        } else {
            Toast.makeText(context, "Permiso de galería denegado", Toast.LENGTH_SHORT).show()
        }
        viewModel.onGalleryPermissionResult(granted) // Notificar al VM
    }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) tempCameraImageUri?.let { viewModel.onImageSelected(it) }
        else Toast.makeText(context, "Captura cancelada", Toast.LENGTH_SHORT).show()
    }
    val cameraPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            ComposeFileProvider.getImageUri(context)?.let { uri ->
                tempCameraImageUri = uri
                cameraLauncher.launch(uri)
            } ?: Toast.makeText(context, "Error al crear archivo", Toast.LENGTH_SHORT).show()
        } else Toast.makeText(context, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show()
    }

    val legacyWritePermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        viewModel.onLegacyWritePermissionResult(granted) // Notificar al VM
    }

    // --- Colector de Eventos del ViewModel ---
    LaunchedEffect(Unit) {
        viewModel.eventFlow.collectLatest { event ->
            when (event) {
                is ProjectDetailEvent.NavigateToProcessing -> onNavigateToProcessing(event.projectId)
                is ProjectDetailEvent.ShowToast -> snackbarHostState.showSnackbar(event.message, duration = SnackbarDuration.Short)
                is ProjectDetailEvent.RequestCameraPermission -> cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                is ProjectDetailEvent.RequestGalleryPermission -> galleryPermissionLauncher.launch(event.permission)
                is ProjectDetailEvent.RequestLegacyWritePermission -> legacyWritePermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(uiState.project?.name ?: "Detalle Proyecto", color = Beige, maxLines = 1) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Teal),
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = Beige)
                    }
                }
            )
        },
        containerColor = Teal
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(Teal)
        ) {
            when {
                uiState.isLoadingProject -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Beige)
                }
                uiState.errorMessage != null && uiState.project == null -> {
                    Column(modifier = Modifier.align(Alignment.Center).padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally){
                        Text(
                            uiState.errorMessage!!,
                            color = Coral,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp)) // Añadir espacio
                        Button(onClick = { viewModel.loadProject(projectId)}){ Text("Reintentar")}
                    }
                }
                uiState.project != null -> {
                    val project = uiState.project!!
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        // --- Detalles del Proyecto ---
                        DetailItem(label = "Descripción", value = project.description)
                        DetailItem(label = "Capital", value = "$ ${String.format("%.2f", project.capital)}")
                        DetailItem(label = "Tipo", value = if (project.isSelfMade) "Autogestionado" else "Con equipo")
                        DetailItem(label = "Creado", value = formatTimestamp(project.createdAt))
                        Spacer(modifier = Modifier.height(20.dp))

                        // --- Sección de Imagen ---
                        Text("Mockup / Imagen Adjunta:", style = MaterialTheme.typography.titleMedium, color = LightGray)
                        Spacer(modifier = Modifier.height(8.dp))

                        val imageToShow = uiState.selectedImageUri ?: project.imageUri?.let { Uri.parse(it) }
                        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp)) {
                            if (imageToShow != null) {
                                Image(
                                    painter = rememberAsyncImagePainter(imageToShow),
                                    contentDescription = "Mockup seleccionado",
                                    modifier = Modifier.fillMaxWidth().height(200.dp),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Box(
                                    modifier = Modifier.fillMaxWidth().height(150.dp).background(GrayBlue.copy(alpha = 0.3f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally){
                                        Icon(Icons.Outlined.ImageNotSupported, contentDescription = null, tint = LightGray, modifier = Modifier.size(40.dp))
                                        Text("Ninguna imagen seleccionada", color = LightGray, fontSize = 14.sp)
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))

                        // --- Botones para seleccionar imagen ---
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Button(onClick = { viewModel.requestGalleryAccess() }, colors = ButtonDefaults.buttonColors(containerColor = Beige, contentColor = Teal)) {
                                Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(ButtonDefaults.IconSize))
                                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                                Text("Galería")
                            }
                            Button(onClick = { viewModel.requestCameraPermission() }, colors = ButtonDefaults.buttonColors(containerColor = Beige, contentColor = Teal)) {
                                Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(ButtonDefaults.IconSize))
                                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                                Text("Cámara")
                            }
                        }
                        Spacer(modifier = Modifier.height(24.dp))

                        // --- Mostrar error específico de acciones ---
                        if (uiState.errorMessage != null) {
                            Text(
                                text = uiState.errorMessage!!,
                                color = Coral,
                                style = MaterialTheme.typography.bodySmall,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                            )
                        }

                        // --- Botones de Acción ---
                        val canRequestQuote = !project.hasPendingQuotation && !uiState.isRequestingQuote
                        val canDownloadQuote = !project.hasPendingQuotation

                        // Botón Solicitar Cotización (Completo)
                        Button(
                            onClick = { viewModel.requestQuotation() },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            enabled = canRequestQuote,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if(canRequestQuote) Coral else GrayBlue,
                                contentColor = Beige,
                                disabledContainerColor = GrayBlue.copy(alpha = 0.7f), // Color deshabilitado
                                disabledContentColor = LightGray.copy(alpha = 0.7f) // Color texto deshabilitado
                            )
                        ) {
                            when {
                                uiState.isRequestingQuote -> CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Beige, strokeWidth = 2.dp)
                                project.hasPendingQuotation -> Text("Cotización Pendiente...", fontWeight = FontWeight.Bold)
                                else -> {
                                    Icon(Icons.Default.Send, contentDescription = null)
                                    Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                                    Text("Solicitar Cotización", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))

                        // Botón Descargar Reporte (Completo)
                        Button(
                            onClick = { viewModel.downloadQuotationReport() },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            enabled = canDownloadQuote,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if(canDownloadQuote) Beige else GrayBlue,
                                contentColor = if(canDownloadQuote) Teal else LightGray,
                                disabledContainerColor = GrayBlue.copy(alpha = 0.7f), // Color deshabilitado
                                disabledContentColor = LightGray.copy(alpha = 0.7f) // Color texto deshabilitado
                            )
                        ) {
                            if (uiState.isDownloadingQuote) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Teal, strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.Default.CloudDownload, contentDescription = null)
                                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                                Text("Descargar Reporte", fontWeight = FontWeight.Bold)
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp)) // Espacio al final

                    } // Fin Column principal
                }
                // Estado inicial/inesperado mientras carga o si project es null pero sin error
                else -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Beige)
                }
            } // Fin when
        } // Fin Box
    } // Fin Scaffold
}

// DetailItem Composable (Completo y sin cambios)
@Composable
fun DetailItem(label: String, value: String) {
    Column(modifier = Modifier.padding(bottom = 8.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = LightGray,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = Beige
        )
        Divider(color = LightGray.copy(alpha = 0.2f), thickness = 0.5.dp, modifier = Modifier.padding(top = 4.dp))
    }
}