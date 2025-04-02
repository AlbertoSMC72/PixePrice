package com.example.pixelprice.features.views.projectDetail.precentation

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.pixelprice.ui.theme.Beige
import com.example.pixelprice.ui.theme.Coral
import com.example.pixelprice.ui.theme.Teal
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.runtime.getValue
import com.example.pixelprice.features.views.createProject.data.model.ProjectDTO

@Composable
fun ProjectDetailScreen(
    viewModel: ProjectDetailViewModel,
    projectId: Int,
    onNavigateToQuote: () -> Unit
) {
    val project by viewModel.project.observeAsState<ProjectDTO?>()
    val error by viewModel.error.observeAsState("")

    // Cargar el proyecto al entrar
    LaunchedEffect(Unit) {
        viewModel.loadProject(projectId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .background(Teal)
    ) {
        if (project != null) {
            Text(
                text = project!!.name,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Beige
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text("Descripción:", color = Beige, fontWeight = FontWeight.SemiBold)
            Text(project!!.description, color = Beige)

            Spacer(modifier = Modifier.height(12.dp))

            Text("Capital disponible: $${project!!.capital}", color = Beige)
            Text(
                text = if (project!!.isSelfMade) "Proyecto autogestionado" else "Proyecto con equipo",
                color = Beige
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text("Fecha de creación: ${project!!.createdAt}", color = Beige)

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    viewModel.requestQuote(projectId)
                    onNavigateToQuote()
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Beige,
                    contentColor = Teal
                )
            ) {
                Text("Solicitar Cotización", fontWeight = FontWeight.Bold)
            }

        } else if (error.isNotEmpty()) {
            Text(text = error, color = Coral)
        } else {
            CircularProgressIndicator(color = Beige)
        }
    }
}
