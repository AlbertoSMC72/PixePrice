package com.example.pixelprice.features.auth.login.presentation

import android.app.Application
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pixelprice.ui.theme.Beige
import com.example.pixelprice.ui.theme.LightGray
import com.example.pixelprice.ui.theme.Teal
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.runtime.* // Importar todo runtime
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext // Para el Toast
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.lifecycle.viewmodel.compose.viewModel // Para obtener VM
import com.example.pixelprice.core.ui.LoginViewModelFactory
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class) // Para OutlinedTextField
@Composable
fun LoginScreen(
    // Obtener VM usando la factory (si no usas Hilt)
    loginViewModel: LoginViewModel = viewModel(factory = LoginViewModelFactory()),
    onNavigate: (String) -> Unit // Sigue recibiendo la lambda para ejecutar navegación
) {
    // Observar el StateFlow de la UI
    val uiState by loginViewModel.uiState.collectAsState()
    val context = LocalContext.current // Para el Toast

    var isPasswordVisible by remember { mutableStateOf(false) }

    // --- Manejo de Eventos (Navegación y Toasts) ---
    LaunchedEffect(key1 = Unit) { // key1 = Unit para que se lance solo una vez
        loginViewModel.navigationEvent.collectLatest { event ->
            when (event) {
                is LoginNavigationEvent.NavigateToProjectList -> onNavigate("ProjectList") // Usa la ruta definida
                is LoginNavigationEvent.NavigateToRegister -> onNavigate("Register")
                else -> {}
            }
        }
    }
    LaunchedEffect(key1 = Unit) {
        loginViewModel.toastEvent.collectLatest { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }
    // ------------------------------------------------

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Teal)
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally // Centrar horizontalmente
    ) {
        Text(
            text = "Inicia Sesión",
            style = MaterialTheme.typography.displaySmall, // Usar estilos de MaterialTheme
            fontWeight = FontWeight.Bold,
            color = Beige,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp) // Más espacio abajo
        )

        // Email TextField
        OutlinedTextField( // Usar OutlinedTextField para consistencia con Register
            value = uiState.email,
            onValueChange = { loginViewModel.onEmailChange(it) },
            label = { Text("Correo Electrónico") },
            leadingIcon = { Icon(Icons.Default.Mail, contentDescription = "Icono Correo", tint = LightGray) },
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            isError = uiState.errorMessage?.contains("correo", ignoreCase = true) == true, // Marcar error si aplica
            colors = TextFieldDefaults.outlinedTextFieldColors( // Colores personalizados
                focusedTextColor = LightGray,
                unfocusedTextColor = Beige,
                focusedBorderColor = Beige,
                unfocusedBorderColor = LightGray,
                cursorColor = Beige,
                focusedLabelColor = Beige,
                unfocusedLabelColor = LightGray,
                errorBorderColor = MaterialTheme.colorScheme.error, // Usar color de error del tema
                errorLabelColor = MaterialTheme.colorScheme.error
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Password TextField
        OutlinedTextField(
            value = uiState.password,
            onValueChange = { loginViewModel.onPasswordChange(it) },
            label = { Text("Contraseña") },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Icono Contraseña", tint = LightGray) },
            trailingIcon = {
                IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                    Icon(
                        imageVector = if (isPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                        contentDescription = if (isPasswordVisible) "Ocultar contraseña" else "Mostrar contraseña",
                        tint = LightGray // Color del icono
                    )
                }
            },
            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            isError = uiState.errorMessage?.contains("contraseña", ignoreCase = true) == true,
            colors = TextFieldDefaults.outlinedTextFieldColors( // Mismos colores
                focusedTextColor = LightGray,
                unfocusedTextColor = Beige,
                focusedBorderColor = Beige,
                unfocusedBorderColor = LightGray,
                cursorColor = Beige,
                focusedLabelColor = Beige,
                unfocusedLabelColor = LightGray,
                errorBorderColor = MaterialTheme.colorScheme.error,
                errorLabelColor = MaterialTheme.colorScheme.error
            )
        )

        Spacer(modifier = Modifier.height(8.dp)) // Menos espacio antes del error

        // Mensaje de Error (si existe)
        if (uiState.errorMessage != null) {
            Text(
                text = uiState.errorMessage!!,
                color = MaterialTheme.colorScheme.error, // Color de error del tema
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp), // Padding horizontal para el texto
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodySmall // Tamaño más pequeño para el error
            )
            Spacer(modifier = Modifier.height(8.dp)) // Espacio después del error
        } else {
            Spacer(modifier = Modifier.height(16.dp)) // Espacio normal si no hay error
        }


        // Login Button
        Button(
            onClick = { loginViewModel.onLoginClick() },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .height(50.dp),
            enabled = !uiState.isLoading, // Deshabilitar si está cargando
            shape = RoundedCornerShape(10.dp), // Borde redondeado
            colors = ButtonDefaults.buttonColors(
                containerColor = Beige,
                contentColor = Teal,
                disabledContainerColor = LightGray.copy(alpha = 0.7f), // Color cuando deshabilitado
                disabledContentColor = Teal.copy(alpha = 0.5f)
            )
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp), // Tamaño del indicador
                    color = Teal, // Color del indicador
                    strokeWidth = 2.dp // Grosor del indicador
                )
            } else {
                Text(
                    text = "Iniciar Sesión",
                    fontSize = 18.sp, // Ajustar tamaño fuente
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp)) // Más espacio antes del link de registro

        // Link para ir a Registro
        Text(
            text = buildAnnotatedString {
                withStyle(
                    style = SpanStyle(
                        color = LightGray
                    )
                ) {append("¿No tienes una cuenta? ")}
                pushStringAnnotation(tag = "REGISTER_LINK", annotation = "register") // Tag para accesibilidad/pruebas
                withStyle(
                    style = SpanStyle(
                        color = Beige,
                        fontWeight = FontWeight.Bold, // Hacerlo negrita
                        textDecoration = TextDecoration.Underline
                    )
                ) {
                    append("Regístrate")
                }
                pop()
            },
            style = MaterialTheme.typography.bodyLarge, // Estilo del tema
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = !uiState.isLoading) { // Clickable, deshabilitado si carga
                    loginViewModel.navigateToRegister()
                }
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}