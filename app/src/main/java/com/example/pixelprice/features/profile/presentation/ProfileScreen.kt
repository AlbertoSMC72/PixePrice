package com.example.pixelprice.features.profile.presentation

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pixelprice.ui.theme.*
import kotlinx.coroutines.flow.collectLatest
import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    userId: Int,
    onLogout: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

// Launcher para el selector de directorios
    val directoryPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree(),
        onResult = { uri: Uri? ->
            viewModel.onDirectorySelected(uri)
        }
    )

    // Cargar perfil al iniciar (si cambia userId)
    LaunchedEffect(userId) {
        if (userId != 0) { // Evitar cargar si el ID es inválido
            viewModel.loadProfile(userId)
        }
    }

    // Manejar eventos del ViewModel
    LaunchedEffect(Unit) {
        viewModel.eventFlow.collectLatest { event ->
            when (event) {
                is ProfileEvent.ShowToast -> snackbarHostState.showSnackbar(
                    event.message,
                    duration = SnackbarDuration.Short
                )

                is ProfileEvent.NavigateToLogin -> onLogout()
                is ProfileEvent.OpenDirectoryPicker -> {
                    try {
                        directoryPickerLauncher.launch(null)
                    } catch (e: Exception) {
                        Log.e("ProfileScreen", "Error al lanzar OpenDocumentTree", e)
                        Toast.makeText(
                            context,
                            "No se pudo abrir el selector de carpetas.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Mi Perfil", color = Beige) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Teal),
                actions = {
                    IconButton(onClick = { viewModel.logout() }) {
                        Icon(
                            Icons.Default.Logout,
                            contentDescription = "Cerrar Sesión",
                            tint = Beige
                        )
                    }
                }
            )
        },
        containerColor = Teal
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(Teal)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when {
                uiState.isLoading -> {
                    Spacer(modifier = Modifier.height(64.dp))
                    CircularProgressIndicator(color = Beige)
                }

                uiState.errorMessage != null && uiState.profileData?.data?.user == null -> {
                    Spacer(modifier = Modifier.height(64.dp))
                    Text(
                        "Error: ${uiState.errorMessage}",
                        color = Coral,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { viewModel.loadProfile(userId) }) { Text("Reintentar") }
                }

                uiState.profileData?.data?.user != null -> { // Mostrar formulario si hay datos
                    Icon(
                        Icons.Default.AccountCircle,
                        contentDescription = null,
                        tint = Beige,
                        modifier = Modifier.size(80.dp)
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    // --- Campos del Perfil ---

                    // Email (Solo Lectura)
                    OutlinedTextField(
                        value = uiState.email,
                        onValueChange = { /* No hacer nada */ },
                        label = { Text("Correo electrónico") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true, // Hacerlo solo lectura
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            disabledTextColor = Beige.copy(alpha = 0.7f), // Color texto deshabilitado
                            disabledBorderColor = LightGray.copy(alpha = 0.5f),
                            disabledLabelColor = LightGray,
                            disabledLeadingIconColor = LightGray
                        )
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Nombre (Editable)
                    OutlinedTextField(
                        value = uiState.name,
                        onValueChange = viewModel::onNameChange,
                        label = { Text("Nombre") },
                        leadingIcon = {
                            Icon(
                                Icons.Default.PersonOutline,
                                contentDescription = null
                            )
                        }, // Icono diferente
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                        colors = profileTextFieldColors() // Usar helper de colores
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Apellidos (Editable)
                    OutlinedTextField(
                        value = uiState.lastName,
                        onValueChange = viewModel::onLastNameChange,
                        label = { Text("Apellidos") },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Badge,
                                contentDescription = null
                            )
                        }, // Icono diferente
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                        colors = profileTextFieldColors() // Usar helper de colores
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    if (uiState.errorMessage != null) {
                        Text(
                            text = uiState.errorMessage!!,
                            color = Coral,
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                        )
                    }

                    Text(
                        "Carpeta de Descarga Reportes:",
                        style = MaterialTheme.typography.titleMedium,
                        color = LightGray,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    val currentPathText = remember(uiState.downloadDirectoryUri) {
                        getDisplayPath(context, uiState.downloadDirectoryUri)
                    }
                    Text(
                        text = currentPathText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Beige.copy(alpha = 0.8f),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Button(
                        onClick = { viewModel.selectDownloadDirectory() },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GrayBlue,
                            contentColor = Beige
                        )
                    ) {
                        Icon(Icons.Default.FolderOpen, contentDescription = null)
                        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                        Text("Seleccionar Carpeta")
                    }
                    Spacer(modifier = Modifier.height(24.dp))


                    // Mostrar error de guardado si existe
                    if (uiState.errorMessage != null && !uiState.isLoading) { // No mostrar error de carga aquí
                        Text(
                            text = uiState.errorMessage!!,
                            color = Coral,
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                        )
                    }

                    Button(
                        onClick = { viewModel.updateProfile(userId) },
                        enabled = !uiState.isSaving,
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Beige,
                            contentColor = Teal
                        )
                    ) {
                        if (uiState.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Teal,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Default.Save, contentDescription = null)
                            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                            Text(
                                "Guardar Cambios",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        }
                    }
                }

                else -> { // Estado inicial mientras carga o si profileData es null sin error
                    Spacer(modifier = Modifier.height(64.dp))
                    Text("Cargando perfil...", color = LightGray)
                }
            } // Fin When
        } // Fin Column
    } // Fin Scaffold
}

// Helper para colores de TextField del perfil
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun profileTextFieldColors() = TextFieldDefaults.outlinedTextFieldColors(
    focusedBorderColor = Beige,
    unfocusedBorderColor = LightGray,
    cursorColor = Beige,
    focusedLabelColor = Beige,
    unfocusedLabelColor = LightGray,
    focusedTextColor = Beige,
    unfocusedTextColor = Beige.copy(alpha = 0.8f),
    focusedLeadingIconColor = Beige, // Color icono enfocado
    unfocusedLeadingIconColor = LightGray // Color icono desenfocado
    // No incluimos colores de error aquí, se manejan globalmente
)

private fun getDisplayPath(context: Context, uriString: String?): String {
    if (uriString == null) return "Predeterminada (Descargas)"
    return try {
        val uri = Uri.parse(uriString)
        // Usar DocumentFile para intentar obtener el nombre real del directorio
        val directory = DocumentFile.fromTreeUri(context, uri)
        if (directory != null && directory.isDirectory) {
            "Seleccionada: '${directory.name}'" // Mostrar nombre real si es posible
        } else {
            Log.w("ProfileScreen", "No se pudo obtener nombre para URI: $uriString")
            "Carpeta Seleccionada (inválida?)"
        }
    } catch (e: Exception) {
        Log.w("ProfileScreen", "Error parseando/obteniendo nombre de URI: $uriString", e)
        "Carpeta Seleccionada (Error)"
    }
}