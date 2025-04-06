package com.example.pixelprice.features.projects.presentation.list

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.* // Importar todos los iconos filled
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pixelprice.features.projects.data.local.ProjectEntity
import com.example.pixelprice.ui.theme.*
import androidx.compose.ui.text.style.TextOverflow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectListScreen(
    viewModel: ProjectListViewModel,
    onNavigateToCreate: () -> Unit,
    onNavigateToDetail: (Int) -> Unit,
    onNavigateToProfile: () -> Unit,
    // Eliminado: onNavigateToQuotationList
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis Proyectos", color = Beige) }, // Título simplificado
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Teal),
                actions = {
                    // Botón para ir al perfil
                    IconButton(onClick = onNavigateToProfile) {
                        Icon(Icons.Default.Person, contentDescription = "Perfil", tint = Beige)
                    }
                    // Botón para refrescar
                    IconButton(onClick = { Log.d("ProjectListVM", "Iniciando flujo de proyectos...") }, enabled = !uiState.isLoading) {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = "Refrescar Proyectos",
                            tint = if (uiState.isLoading) LightGray else Beige
                        )
                    }
                    // Eliminado: IconButton(onClick = onNavigateToQuotationList)
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToCreate,
                containerColor = Beige,
                contentColor = Teal
            ) {
                Icon(Icons.Default.Add, contentDescription = "Crear Nuevo Proyecto")
            }
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
                uiState.isLoading && uiState.projects.isEmpty() -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = Beige
                    )
                }
                uiState.errorMessage != null -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center).padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = uiState.errorMessage!!,
                            color = Coral,
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { Log.d("ProjectListVM", "Iniciando flujo de proyectos...") }) {
                            Text("Reintentar")
                        }
                    }
                }
                !uiState.isLoading && uiState.projects.isEmpty() -> {
                    Text(
                        text = "No tienes proyectos.\nCrea uno con (+).",
                        color = LightGray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.align(Alignment.Center).padding(32.dp),
                        style = MaterialTheme.typography.headlineSmall
                    )
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(uiState.projects, key = { project -> project.id }) { project ->
                            ProjectCard( // Usar el ProjectCard definido abajo
                                project = project,
                                onClick = { onNavigateToDetail(project.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProjectCard(project: ProjectEntity, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = Beige,
            contentColor = Teal
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically // Alinear verticalmente
        ) {
            // Columna para texto (ocupa espacio disponible)
            Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                Text(
                    text = project.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis // Cortar nombre largo
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = project.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = GrayBlue,
                    maxLines = 2, // Limitar descripción
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(8.dp))
                // Mostrar estado de cotización
                val statusText: String
                val statusColor: androidx.compose.ui.graphics.Color
                when {
                    project.hasPendingQuotation -> {
                        statusText = "Cotización Pendiente..."
                        statusColor = GrayBlue
                    }
                    project.lastQuotationId != null -> {
                        statusText = "Cotización Lista"
                        statusColor = MaterialTheme.colorScheme.primary // Verde o azul del tema
                    }
                    else -> {
                        statusText = "Sin Cotizar"
                        statusColor = LightGray
                    }
                }
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.labelSmall, // Etiqueta pequeña
                    color = statusColor,
                    fontWeight = FontWeight.Medium
                )
            }
            // Icono indicativo a la derecha
            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = "Ver detalles",
                tint = Teal.copy(alpha = 0.7f) // Un poco más tenue
            )
        }
    }
}