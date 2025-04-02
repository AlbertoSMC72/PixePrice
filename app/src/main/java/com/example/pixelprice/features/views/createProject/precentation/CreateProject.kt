package com.example.pixelprice.features.views.createProject.precentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.pixelprice.ui.theme.Teal
import com.example.pixelprice.ui.theme.Beige
import com.example.pixelprice.ui.theme.Coral

@Composable
fun CreateProjectScreen(
    viewModel: CreateProjectViewModel,
    onProjectCreated: () -> Unit
) {
    val name by viewModel.name.observeAsState("")
    val description by viewModel.description.observeAsState("")
    val capital by viewModel.capital.observeAsState("")
    val isSelfMade by viewModel.isSelfMade.observeAsState(false)
    val error by viewModel.error.observeAsState("")
    val success by viewModel.success.observeAsState(false)

    if (success) {
        LaunchedEffect(Unit) {
            onProjectCreated()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .background(Teal),
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = "Nuevo Proyecto",
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            color = Beige,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        TextField(
            value = name,
            onValueChange = { viewModel.onNameChanged(it) },
            label = { Text("Nombre del Proyecto") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        TextField(
            value = description,
            onValueChange = { viewModel.onDescriptionChanged(it) },
            label = { Text("Descripción") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        TextField(
            value = capital,
            onValueChange = { viewModel.onCapitalChanged(it) },
            label = { Text("Capital Disponible") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = isSelfMade,
                onCheckedChange = { viewModel.onSelfMadeChanged(it) }
            )
            Text(text = "¿Proyecto autogestionado?", color = Beige)
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (error.isNotEmpty()) {
            Text(text = error, color = Coral)
            Spacer(modifier = Modifier.height(8.dp))
        }

        Button(
            onClick = { viewModel.onCreateProject() },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Beige,
                contentColor = Teal
            )
        ) {
            Text("Crear Proyecto", fontWeight = FontWeight.Bold)
        }
    }
}
