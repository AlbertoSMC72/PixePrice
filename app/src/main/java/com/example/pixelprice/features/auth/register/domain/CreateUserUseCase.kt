package com.example.pixelprice.features.auth.register.domain

import com.example.pixelprice.features.auth.register.data.model.CreateUserRequest
import com.example.pixelprice.features.auth.register.data.model.UserDTO
import com.example.pixelprice.features.auth.register.data.repository.RegisterRepository

class CreateUserUseCase {
    private val repository = RegisterRepository()

    suspend operator fun invoke(user: CreateUserRequest) : Result<UserDTO> {
        return repository.createUser(user)
    }
}