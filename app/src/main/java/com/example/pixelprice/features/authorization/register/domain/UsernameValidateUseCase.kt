package com.example.pixelprice.features.authorization.register.domain

import com.example.pixelprice.features.authorization.register.data.model.UsernameValidateDTO
import com.example.pixelprice.features.authorization.register.data.repository.RegisterRepository

class UsernameValidateUseCase {
    private  val repository = RegisterRepository()

    suspend operator fun invoke() : Result<UsernameValidateDTO> {
        val result  = repository.validateUsername()

        // En caso de existir acá debe estar la lógica de negocio
        return result
    }
}