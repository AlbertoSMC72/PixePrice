package com.example.pixelprice.features.authorization.register.domain

import com.example.pixelprice.features.authorization.register.data.model.CreateUserRequest
import com.example.pixelprice.features.authorization.register.data.model.UserDTO
import com.example.pixelprice.features.authorization.register.data.repository.RegisterRepository

class CreateUserUSeCase {
    private  val repository = RegisterRepository()

    suspend operator fun invoke(user: CreateUserRequest) : Result<UserDTO> {
        val result = repository.createUser(user)

        return result
    }
}