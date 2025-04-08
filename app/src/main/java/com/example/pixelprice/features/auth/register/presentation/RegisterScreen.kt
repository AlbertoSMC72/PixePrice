package com.example.pixelprice.features.auth.register.presentation

import android.app.Application
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pixelprice.core.ui.RegisterViewModelFactory
import com.example.pixelprice.ui.theme.*
import kotlinx.coroutines.flow.collectLatest


@Preview(showBackground = true, backgroundColor = 0xFF387780)
@Composable
fun PreviewRegisterScreen() {
    val context = LocalContext.current
    RegisterScreen(
        registerViewModel = viewModel(factory = RegisterViewModelFactory()),
        onNavigate = { dest -> println("Preview Navigate to: $dest") }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    registerViewModel: RegisterViewModel = viewModel(factory = RegisterViewModelFactory()),
    onNavigate: (String) -> Unit
) {
    val uiState by registerViewModel.uiState.collectAsState()
    val context = LocalContext.current
    var isPasswordVisible by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = Unit) {
        registerViewModel.navigationEvent.collectLatest { event ->
            when (event) {
                is RegisterNavigationEvent.NavigateToLogin -> onNavigate("Login")
            }
        }
    }
    LaunchedEffect(key1 = Unit) {
        registerViewModel.toastEvent.collectLatest { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Teal)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Regístrate",
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
            color = Beige,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp)
        )

        OutlinedTextField(
            value = uiState.name,
            onValueChange = registerViewModel::onChangeName,
            label = { Text("Nombre(s)") },
            leadingIcon = { Icon(Icons.Default.Person, contentDescription = "Icono Usuario", tint = LightGray) },
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            isError = uiState.errorMessage?.contains("usuario", ignoreCase = true) == true,
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedTextColor = LightGray,
                unfocusedTextColor = Beige,
                focusedBorderColor = Beige, unfocusedBorderColor = LightGray, cursorColor = Beige,
                focusedLabelColor = Beige, unfocusedLabelColor = LightGray,
                errorBorderColor = Coral, errorLabelColor = Coral
            )
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = uiState.lastName,
            onValueChange = registerViewModel::onChangeLastName,
            label = { Text("Apellido(s)") },
            leadingIcon = { Icon(Icons.Default.Person, contentDescription = "Icono Usuario", tint = LightGray) },
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            isError = uiState.errorMessage?.contains("usuario", ignoreCase = true) == true,
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedTextColor = LightGray,
                unfocusedTextColor = Beige,
                focusedBorderColor = Beige, unfocusedBorderColor = LightGray, cursorColor = Beige,
                focusedLabelColor = Beige, unfocusedLabelColor = LightGray,
                errorBorderColor = Coral, errorLabelColor = Coral
            )
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = uiState.email,
            onValueChange = registerViewModel::onChangeEmail,
            label = { Text("Correo Electrónico") },
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = "Icono Correo", tint = LightGray) },
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            isError = uiState.errorMessage?.contains("correo", ignoreCase = true) == true,
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedTextColor = LightGray,
                unfocusedTextColor = Beige,
                focusedBorderColor = Beige, unfocusedBorderColor = LightGray, cursorColor = Beige,
                focusedLabelColor = Beige, unfocusedLabelColor = LightGray,
                errorBorderColor = Coral, errorLabelColor = Coral
            )
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = uiState.password,
            onValueChange = registerViewModel::onChangePassword,
            label = { Text("Contraseña (mín. 6 caracteres)") },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Icono Contraseña", tint = LightGray) },
            trailingIcon = {
                IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                    Icon(
                        imageVector = if (isPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                        contentDescription = if (isPasswordVisible) "Ocultar contraseña" else "Mostrar contraseña",
                        tint = LightGray
                    )
                }
            },
            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            isError = uiState.errorMessage?.contains("contraseña", ignoreCase = true) == true,
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedTextColor = LightGray,
                unfocusedTextColor = Beige,
                focusedBorderColor = Beige, unfocusedBorderColor = LightGray, cursorColor = Beige,
                focusedLabelColor = Beige, unfocusedLabelColor = LightGray,
                errorBorderColor = Coral, errorLabelColor = Coral
            )
        )
        Spacer(modifier = Modifier.height(8.dp))

        if (uiState.errorMessage != null) {
            Text(
                text = uiState.errorMessage!!,
                color = Coral,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(modifier = Modifier.height(8.dp))
        } else {
            Spacer(modifier = Modifier.height(16.dp))
        }

        Button(
            onClick = registerViewModel::onRegisterClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .height(50.dp),
            enabled = !uiState.isLoading,
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Beige, contentColor = Teal,
                disabledContainerColor = LightGray.copy(alpha = 0.7f),
                disabledContentColor = Teal.copy(alpha = 0.5f)
            )
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Teal,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = "Crear Cuenta",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = buildAnnotatedString {
                withStyle(
                    style = SpanStyle(
                        color = LightGray
                    )
                ) { append("¿Ya tienes una cuenta? ") }
                pushStringAnnotation(tag = "LOGIN_LINK", annotation = "login")
                withStyle(
                    style = SpanStyle(
                        color = Beige,
                        fontWeight = FontWeight.Bold,
                        textDecoration = TextDecoration.Underline
                    )
                ) {
                    append("Inicia Sesión")
                }
                pop()
            },
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = !uiState.isLoading) { registerViewModel.navigateToLogin() }
        )
        Spacer(modifier = Modifier.height(16.dp))
    }
}