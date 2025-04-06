package com.example.pixelprice.features.profile.presentation

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pixelprice.ui.theme.*
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    userId: Int,
    onLogout: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(userId) {
        viewModel.loadProfile(userId)
    }

    LaunchedEffect(Unit) {
        viewModel.eventFlow.collectLatest { event ->
            when (event) {
                is ProfileEvent.ShowToast -> snackbarHostState.showSnackbar(event.message, duration = SnackbarDuration.Short)
                is ProfileEvent.NavigateToLogin -> onLogout()
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
                        Icon(Icons.Default.Logout, contentDescription = "Cerrar Sesión", tint = Beige)
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
                uiState.errorMessage != null && uiState.profileData == null -> { // Error de carga inicial
                    Spacer(modifier = Modifier.height(64.dp))
                    Text("Error: ${uiState.errorMessage}", color = Coral, textAlign = TextAlign.Center)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { viewModel.loadProfile(userId) }) { Text("Reintentar") }
                }
                uiState.profileData != null -> { // Mostrar formulario si hay datos
                    Icon(Icons.Default.AccountCircle, contentDescription = null, tint=Beige, modifier = Modifier.size(80.dp))
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
                        leadingIcon = { Icon(Icons.Default.PersonOutline, contentDescription = null) }, // Icono diferente
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
                        leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null) }, // Icono diferente
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                        colors = profileTextFieldColors() // Usar helper de colores
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    // Mostrar error de guardado si existe
                    if(uiState.errorMessage != null) {
                        Text(
                            text = uiState.errorMessage!!,
                            color = Coral,
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                        )
                    }

                    // --- Botón Guardar Cambios ---
                    Button(
                        onClick = { viewModel.updateProfile(userId) },
                        enabled = !uiState.isSaving,
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Beige, contentColor = Teal)
                    ) {
                        if(uiState.isSaving) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Teal, strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.Save, contentDescription = null)
                            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                            Text("Guardar Cambios", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        }
                    }
                }
                else -> {
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