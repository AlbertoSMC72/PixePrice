package com.example.pixelprice.features.authorization.login.domain


import com.example.pixelprice.features.authorization.login.data.repository.AuthRepository

class LoginUseCase {
    private val authRepository = AuthRepository()

    suspend operator fun invoke(email: String, password: String): Result<String> {
        return authRepository.login(email, password)
    }
}

