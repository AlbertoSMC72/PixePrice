package com.example.pixelprice.features.projects.presentation.create

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pixelprice.ui.theme.*
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateProjectScreen(
    viewModel: CreateProjectViewModel,
    onProjectCreated: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // --- Estados locales para los nuevos campos ---
    var projectPurpose by remember { mutableStateOf("") } // Reemplaza la descripción general
    var platformType by remember { mutableStateOf("") }
    var techStack by remember { mutableStateOf("") }
    var integrations by remember { mutableStateOf("") }
    var securityReqs by remember { mutableStateOf("") }
    var scalabilityNeeds by remember { mutableStateOf("") }
    var infrastructurePref by remember { mutableStateOf("") }
    var mainFeatures by remember { mutableStateOf("") }
    var userRoles by remember { mutableStateOf("") }
    var screenCount by remember { mutableStateOf("") }
    var reportingNeeds by remember { mutableStateOf("") }
    var premiumFeatures by remember { mutableStateOf("") }
    var maintenanceNeeds by remember { mutableStateOf("") }
    var accessibilityNeeds by remember { mutableStateOf("") }
    var compatibilityNeeds by remember { mutableStateOf("") }
    var requiredLanguages by remember { mutableStateOf("") }
    // El nombre, capital y selfMade ya están en uiState

    LaunchedEffect(Unit) {
        viewModel.eventFlow.collectLatest { event ->
            when (event) {
                is CreateProjectEvent.NavigateBack -> onProjectCreated()
                is CreateProjectEvent.ShowToast -> Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nuevo Proyecto Detallado", color = Beige) }, // Título actualizado
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Teal),
                navigationIcon = {
                    IconButton(onClick = onProjectCreated) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Cancelar", tint = Beige)
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
                .verticalScroll(rememberScrollState()), // Esencial con tantos campos
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Detalles del Proyecto",
                style = MaterialTheme.typography.headlineSmall,
                color = Beige,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // --- Campos Detallados ---

            // Nombre (del ViewModel)
            OutlinedTextField(
                value = uiState.name,
                onValueChange = viewModel::onNameChange,
                label = { Text("Nombre del Proyecto *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = uiState.validationError?.type == ValidationErrorType.NAME,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                colors = createTextFieldColors(),
                supportingText = { if (uiState.validationError?.type == ValidationErrorType.NAME) Text(
                    uiState.validationError!!.message, color = Coral) }
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Propósito/Descripción (anterior 'description')
            OutlinedTextField(
                value = projectPurpose,
                onValueChange = { projectPurpose = it }, // Actualiza estado local
                label = { Text("Propósito del Proyecto *") },
                modifier = Modifier.fillMaxWidth().heightIn(min = 80.dp), // Un poco más pequeño
                isError = uiState.validationError?.type == ValidationErrorType.DESCRIPTION, // Reutilizar validación si aplica
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                maxLines = 4,
                colors = createTextFieldColors(),
                // Mostrar error si el ViewModel lo marca (asume que valida el propósito ahora)
                supportingText = { if (uiState.validationError?.type == ValidationErrorType.DESCRIPTION) Text(uiState.validationError!!.message, color = Coral) }
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Presupuesto (del ViewModel)
            OutlinedTextField(
                value = uiState.capital,
                onValueChange = viewModel::onCapitalChange,
                label = { Text("Presupuesto Estimado ($ USD) *") }, // Cambiar etiqueta
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), // Corregido
                isError = uiState.validationError?.type == ValidationErrorType.CAPITAL,
                colors = createTextFieldColors(),
                prefix = { Text("$ ", color = if (uiState.validationError?.type == ValidationErrorType.CAPITAL) Coral else LightGray) },
                supportingText = { if (uiState.validationError?.type == ValidationErrorType.CAPITAL) Text(
                    uiState.validationError!!.message, color = Coral) }
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Tipo de Plataforma
            OutlinedTextField( value = platformType, onValueChange = { platformType = it }, label = { Text("Tipo Plataforma (Web, Móvil, etc.)") }, modifier = Modifier.fillMaxWidth(), singleLine = true, colors = createTextFieldColors() )
            Spacer(modifier = Modifier.height(12.dp))

            // Stack Tecnológico
            OutlinedTextField( value = techStack, onValueChange = { techStack = it }, label = { Text("Stack Tecnológico (Opcional)") }, modifier = Modifier.fillMaxWidth(), singleLine = true, colors = createTextFieldColors() )
            Spacer(modifier = Modifier.height(12.dp))

            // Integraciones
            OutlinedTextField( value = integrations, onValueChange = { integrations = it }, label = { Text("Integraciones Necesarias") }, modifier = Modifier.fillMaxWidth().heightIn(min=80.dp), maxLines=3, colors = createTextFieldColors() )
            Spacer(modifier = Modifier.height(12.dp))

            // Requerimientos Seguridad
            OutlinedTextField( value = securityReqs, onValueChange = { securityReqs = it }, label = { Text("Requerimientos de Seguridad") }, modifier = Modifier.fillMaxWidth().heightIn(min=80.dp), maxLines=3, colors = createTextFieldColors() )
            Spacer(modifier = Modifier.height(12.dp))

            // Escalabilidad
            OutlinedTextField( value = scalabilityNeeds, onValueChange = { scalabilityNeeds = it }, label = { Text("Necesidades de Escalabilidad") }, modifier = Modifier.fillMaxWidth().heightIn(min=80.dp), maxLines=3, colors = createTextFieldColors() )
            Spacer(modifier = Modifier.height(12.dp))

            // Infraestructura Preferida
            OutlinedTextField( value = infrastructurePref, onValueChange = { infrastructurePref = it }, label = { Text("Infraestructura Preferida (Cloud, etc.)") }, modifier = Modifier.fillMaxWidth(), singleLine = true, colors = createTextFieldColors() )
            Spacer(modifier = Modifier.height(12.dp))

            // Funcionalidades Principales
            OutlinedTextField( value = mainFeatures, onValueChange = { mainFeatures = it }, label = { Text("Funcionalidades Principales (Listar)") }, modifier = Modifier.fillMaxWidth().heightIn(min=120.dp), maxLines=6, colors = createTextFieldColors() )
            Spacer(modifier = Modifier.height(12.dp))

            // Roles de Usuario
            OutlinedTextField( value = userRoles, onValueChange = { userRoles = it }, label = { Text("Roles de Usuario (Listar)") }, modifier = Modifier.fillMaxWidth().heightIn(min=80.dp), maxLines=4, colors = createTextFieldColors() )
            Spacer(modifier = Modifier.height(12.dp))

            // Número de Pantallas
            OutlinedTextField( value = screenCount, onValueChange = { screenCount = it }, label = { Text("Número Aprox. de Pantallas/Vistas") }, modifier = Modifier.fillMaxWidth(), singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), colors = createTextFieldColors() )
            Spacer(modifier = Modifier.height(12.dp))

            // Requerimientos Reportes
            OutlinedTextField( value = reportingNeeds, onValueChange = { reportingNeeds = it }, label = { Text("Requerimientos de Reportes/Análisis") }, modifier = Modifier.fillMaxWidth().heightIn(min=80.dp), maxLines=3, colors = createTextFieldColors() )
            Spacer(modifier = Modifier.height(12.dp))

            // Características Premium
            OutlinedTextField( value = premiumFeatures, onValueChange = { premiumFeatures = it }, label = { Text("Características Premium/Especiales") }, modifier = Modifier.fillMaxWidth().heightIn(min=80.dp), maxLines=3, colors = createTextFieldColors() )
            Spacer(modifier = Modifier.height(12.dp))

            // Mantenimiento
            OutlinedTextField( value = maintenanceNeeds, onValueChange = { maintenanceNeeds = it }, label = { Text("Necesidad de Mantenimiento Posterior") }, modifier = Modifier.fillMaxWidth().heightIn(min=80.dp), maxLines=3, colors = createTextFieldColors() )
            Spacer(modifier = Modifier.height(12.dp))

            // Accesibilidad
            OutlinedTextField( value = accessibilityNeeds, onValueChange = { accessibilityNeeds = it }, label = { Text("Necesidades de Accesibilidad (WCAG, etc.)") }, modifier = Modifier.fillMaxWidth().heightIn(min=80.dp), maxLines=3, colors = createTextFieldColors() )
            Spacer(modifier = Modifier.height(12.dp))

            // Compatibilidad
            OutlinedTextField( value = compatibilityNeeds, onValueChange = { compatibilityNeeds = it }, label = { Text("Compatibilidad (Dispositivos/Navegadores)") }, modifier = Modifier.fillMaxWidth().heightIn(min=80.dp), maxLines=3, colors = createTextFieldColors() )
            Spacer(modifier = Modifier.height(12.dp))

            // Idiomas
            OutlinedTextField( value = requiredLanguages, onValueChange = { requiredLanguages = it }, label = { Text("Idiomas Requeridos (Ej: ES, EN)") }, modifier = Modifier.fillMaxWidth(), singleLine = true, colors = createTextFieldColors() )
            Spacer(modifier = Modifier.height(12.dp))

            // Checkbox Autogestionado (del ViewModel)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox( checked = uiState.isSelfMade, onCheckedChange = viewModel::onSelfMadeChange, /* ... colores ... */ )
                Text(text = "¿Proyecto autogestionado?", color = Beige)
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Mensaje de error general (del ViewModel)
            if (uiState.generalErrorMessage != null) {
                Text(
                    text = uiState.generalErrorMessage!!,
                    color = Coral,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            // Botón Guardar
            Button(
                // *** MODIFICADO onClick ***
                onClick = {
                    // 1. Concatenar todos los campos en una sola descripción
                    val fullDescription = buildString {
                        append("Propósito: ${projectPurpose.trim()}\n")
                        if (platformType.isNotBlank()) append("Plataforma: ${platformType.trim()}\n")
                        if (techStack.isNotBlank()) append("Stack: ${techStack.trim()}\n")
                        if (integrations.isNotBlank()) append("Integraciones: ${integrations.trim()}\n")
                        if (securityReqs.isNotBlank()) append("Seguridad: ${securityReqs.trim()}\n")
                        if (scalabilityNeeds.isNotBlank()) append("Escalabilidad: ${scalabilityNeeds.trim()}\n")
                        if (infrastructurePref.isNotBlank()) append("Infraestructura: ${infrastructurePref.trim()}\n")
                        if (mainFeatures.isNotBlank()) append("Funcionalidades: ${mainFeatures.trim()}\n")
                        if (userRoles.isNotBlank()) append("Roles: ${userRoles.trim()}\n")
                        if (screenCount.isNotBlank()) append("Pantallas: ${screenCount.trim()}\n")
                        if (reportingNeeds.isNotBlank()) append("Reportes: ${reportingNeeds.trim()}\n")
                        if (premiumFeatures.isNotBlank()) append("Premium: ${premiumFeatures.trim()}\n")
                        if (maintenanceNeeds.isNotBlank()) append("Mantenimiento: ${maintenanceNeeds.trim()}\n")
                        if (accessibilityNeeds.isNotBlank()) append("Accesibilidad: ${accessibilityNeeds.trim()}\n")
                        if (compatibilityNeeds.isNotBlank()) append("Compatibilidad: ${compatibilityNeeds.trim()}\n")
                        if (requiredLanguages.isNotBlank()) append("Idiomas: ${requiredLanguages.trim()}")
                    }.trim() // Eliminar salto de línea final si existe

                    // 2. Actualizar el estado de descripción en el ViewModel (si es necesario validarlo)
                    //    O pasarla directamente al método createProject si no se valida
                    viewModel.onDescriptionChange(fullDescription) // Actualiza el estado antes de llamar a createProject

                    // 3. Llamar a la función del ViewModel para crear/guardar
                    viewModel.createProject()
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled = !uiState.isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = Beige, contentColor = Teal)
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Teal, strokeWidth = 2.dp)
                } else {
                    Icon(Icons.Default.Save, contentDescription = null)
                    Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                    Text("Guardar Proyecto", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            }
            Spacer(modifier = Modifier.height(16.dp)) // Espacio final
        } // Fin Column
    } // Fin Scaffold
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun createTextFieldColors() = TextFieldDefaults.outlinedTextFieldColors(
    focusedBorderColor = Beige,
    unfocusedBorderColor = LightGray,
    cursorColor = Beige,
    focusedLabelColor = Beige,
    unfocusedLabelColor = LightGray,
    errorBorderColor = Coral,
    errorLabelColor = Coral,
    focusedTextColor = Beige, // Color del texto al escribir
    unfocusedTextColor = Beige.copy(alpha = 0.8f),
    errorTextColor = Coral,
    focusedSupportingTextColor = Coral, // Color texto de soporte/error enfocado
    unfocusedSupportingTextColor = Coral.copy(alpha = 0.7f),
    errorSupportingTextColor = Coral // Color texto de soporte/error
)