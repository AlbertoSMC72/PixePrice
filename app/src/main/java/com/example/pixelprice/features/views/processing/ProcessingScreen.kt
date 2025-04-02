package com.example.pixelprice.features.views.processing

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import com.example.pixelprice.ui.theme.Teal
import com.example.pixelprice.ui.theme.Beige

@Composable
fun ProcessingScreen(
    projectId: Int,
    onNavigateToQuote: () -> Unit
) {
    LaunchedEffect(Unit) {
        delay(3000) // Simula tiempo de espera
        onNavigateToQuote()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Teal),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = Beige)
            Spacer(modifier = Modifier.height(16.dp))
            Text("Generando cotización...", fontSize = 18.sp, color = Beige)
        }
    }
}