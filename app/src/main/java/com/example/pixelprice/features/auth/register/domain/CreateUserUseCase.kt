package com.example.pixelprice.features.auth.register.domain

// import android.content.Context // Ya no es necesario
import com.example.pixelprice.features.auth.register.data.model.CreateUserRequest
import com.example.pixelprice.features.auth.register.data.model.UserDTO
import com.example.pixelprice.features.auth.register.data.repository.RegisterRepository

// *** CORREGIDO: Ya no necesita Context ***
class CreateUserUseCase { // Quitar parámetro del constructor
    // *** CORREGIDO: Instanciar RegisterRepository sin contexto ***
    private val repository = RegisterRepository()

    suspend operator fun invoke(user: CreateUserRequest) : Result<UserDTO> {
        // Validaciones de negocio (si las hubiera) irían aquí
        // Por ahora, simplemente delega al repositorio
        return repository.createUser(user)
    }
}