package com.example.pixelprice.features.auth.login.domain

// import android.content.Context // Ya no es necesario
import com.example.pixelprice.features.auth.login.data.repository.AuthRepository

// *** CORREGIDO: Ya no necesita Context ***
class LoginUseCase { // Quitar parámetro del constructor
    // *** CORREGIDO: Instanciar AuthRepository sin contexto ***
    private val authRepository = AuthRepository()

    suspend operator fun invoke(email: String, password: String): Result<Unit> {
        // El UseCase simplemente delega la llamada al repositorio
        // Podría añadir lógica de negocio adicional aquí si fuera necesario en el futuro
        return authRepository.login(email, password)
    }
}