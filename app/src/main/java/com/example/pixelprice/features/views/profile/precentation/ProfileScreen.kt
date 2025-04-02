package com.example.pixelprice.features.views.profile.precentation

import androidx.compose.runtime.livedata.observeAsState
import com.example.pixelprice.features.views.profile.data.model.UpdateProfileRequest
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.example.pixelprice.ui.theme.Teal
import com.example.pixelprice.ui.theme.Beige
import com.example.pixelprice.ui.theme.Coral

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    userId: Int
) {
    val profile by viewModel.profile.observeAsState()
    val error by viewModel.error.observeAsState("")

    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }

    // Carga inicial
    LaunchedEffect(Unit) {
        viewModel.loadProfile(userId)
    }

    // Actualiza valores al obtener perfil
    LaunchedEffect(profile) {
        profile?.let {
            username = it.username
            email = it.email
            phone = it.phone.orEmpty()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Teal)
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Tu perfil",
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold,
            color = Beige,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        TextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Nombre de usuario") },
            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Correo electrónico") },
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text("Teléfono") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (error.isNotEmpty()) {
            Text(
                text = error,
                color = Coral,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        Button(
            onClick = {
                val request = UpdateProfileRequest(
                    username = username,
                    email = email,
                    phone = phone
                )
                viewModel.updateProfile(userId, request)
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = Beige,
                contentColor = Teal
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text("Guardar cambios", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        }
    }
}
