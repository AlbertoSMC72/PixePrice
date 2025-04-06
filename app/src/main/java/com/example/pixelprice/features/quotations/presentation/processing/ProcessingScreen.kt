package com.example.pixelprice.features.quotations.presentation.processing

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pixelprice.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProcessingScreen(
    projectId: Int, // Recibe ID solo para contexto, no se usa activamente aquí
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                // Título genérico ya que no tenemos los detalles aquí
                title = { Text("Solicitud Enviada", color = Beige) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Teal),
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver a Detalles", tint = Beige)
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
                .background(Teal),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(32.dp)
            ) {
                Icon(
                    Icons.Default.HourglassTop,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                    tint = Beige
                )
                Spacer(modifier = Modifier.height(24.dp))
                CircularProgressIndicator(color = Beige, modifier = Modifier.size(60.dp))
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    "Solicitud enviada correctamente.",
                    fontSize = 20.sp, // Un poco más pequeño
                    fontWeight = FontWeight.Bold,
                    color = Beige,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "Estamos procesando tu cotización. Recibirás una notificación cuando esté lista.\n\nPuedes volver a la pantalla anterior.",
                    fontSize = 16.sp,
                    color = LightGray,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    onClick = onNavigateBack,
                    colors = ButtonDefaults.buttonColors(containerColor = LightGray, contentColor = Teal)
                ) {
                    Text("Volver a Detalles")
                }
            }
        }
    }
}