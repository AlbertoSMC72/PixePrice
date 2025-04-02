package com.example.pixelprice.features.authorization.login.presentation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pixelprice.core.data.UserInfoProvider
import com.example.pixelprice.features.authorization.login.domain.LoginUseCase
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {
    private val loginUseCase = LoginUseCase()

    private val _email = MutableLiveData("")
    val email: LiveData<String> = _email

    private val _password = MutableLiveData("")
    val password: LiveData<String> = _password

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _navigationCommand = MutableLiveData<String?>()
    val navigationCommand: LiveData<String?> = _navigationCommand

    fun onUsernameChange(newUsername: String) {
        _email.value = newUsername
    }

    fun onPasswordChange(newPassword: String) {
        _password.value = newPassword
    }

    fun onLoginClick() {
        val emailValue = _email.value.orEmpty()
        val passwordValue = _password.value.orEmpty()

        if (emailValue.isBlank() || passwordValue.isBlank()) {
            _errorMessage.value = "Todos los campos son obligatorios"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            val result = loginUseCase(emailValue, passwordValue)
            result.onSuccess { username ->
                _errorMessage.value = null
                _navigationCommand.value = "Home"
            }.onFailure { exception ->
                _errorMessage.value = exception.message ?: "Error desconocido"
            }
            _isLoading.value = false
        }
    }

    fun navigateToDownloads() {
        _navigationCommand.value = "Home"
    }

    fun onNavigationHandled() {
        _navigationCommand.value = null
    }

    fun navigateToRegister() {
        _navigationCommand.value = "Register"
    }

}