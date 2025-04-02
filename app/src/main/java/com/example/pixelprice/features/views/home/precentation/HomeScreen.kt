package com.example.pixelprice.features.views.home.precentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pixelprice.ui.theme.Teal
import com.example.pixelprice.ui.theme.Beige
import com.example.pixelprice.features.views.home.data.model.ProjectDTO

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToCreate: () -> Unit,
    onNavigateToDetail: (Int) -> Unit
) {
    val projects by viewModel.projects.observeAsState(emptyList())
    val error by viewModel.error.observeAsState("")

    LaunchedEffect(Unit) {
        viewModel.loadProjects()
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onNavigateToCreate() },
                containerColor = Beige,
                contentColor = Teal
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nuevo Proyecto")
            }
        },
        containerColor = Teal
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Teal)
        ) {
            Text(
                text = "Tus Proyectos",
                fontSize = 30.sp,
                modifier = Modifier.padding(16.dp),
                color = Beige
            )

            if (error.isNotEmpty()) {
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(16.dp)
                )
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                items(projects) { project ->
                    ProjectItem(project = project) {
                        onNavigateToDetail(project.id)
                    }
                }
            }
        }
    }
}


@Composable
fun ProjectItem(project: ProjectDTO, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = Beige,
            contentColor = Teal
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = project.name, fontSize = 20.sp, fontWeight = Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = project.description.take(100) + "...", fontSize = 14.sp)
        }
    }
}
