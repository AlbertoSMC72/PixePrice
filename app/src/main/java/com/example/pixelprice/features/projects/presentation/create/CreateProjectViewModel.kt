package com.example.pixelprice.features.projects.presentation.create

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.pixelprice.features.projects.data.repository.ProjectException
import com.example.pixelprice.features.projects.domain.usecases.CreateProjectUseCase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class ValidationErrorType { NAME, DESCRIPTION, CAPITAL }
data class ValidationError(val type: ValidationErrorType, val message: String)

data class CreateProjectUiState(
    val name: String = "",
    val description: String = "",
    val capital: String = "",
    val isSelfMade: Boolean = false,
    val isLoading: Boolean = false,
    val validationError: ValidationError? = null,
    val generalErrorMessage: String? = null
)

sealed class CreateProjectEvent {
    object NavigateBack : CreateProjectEvent()
    data class ShowToast(val message: String) : CreateProjectEvent()
}

class CreateProjectViewModel(application: Application) : AndroidViewModel(application) {

    private val createProjectUseCase = CreateProjectUseCase()

    private val _uiState = MutableStateFlow(CreateProjectUiState())
    val uiState: StateFlow<CreateProjectUiState> = _uiState.asStateFlow()

    private val _eventFlow = MutableSharedFlow<CreateProjectEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    fun onNameChange(newName: String) {
        _uiState.update { it.copy(name = newName, validationError = null, generalErrorMessage = null) }
    }
    fun onDescriptionChange(newDesc: String) {
        _uiState.update { it.copy(description = newDesc, validationError = null, generalErrorMessage = null) }
    }
    fun onCapitalChange(newCapital: String) {
        if (newCapital.isEmpty() || (newCapital.count { it == '.' } <= 1 && newCapital.all { it.isDigit() || it == '.' })) {
            _uiState.update { it.copy(capital = newCapital, validationError = null, generalErrorMessage = null) }
        } else {
        }
    }
    fun onSelfMadeChange(checked: Boolean) {
        _uiState.update { it.copy(isSelfMade = checked) }
    }

    private fun validateInputs(): Boolean {
        val state = _uiState.value
        var isValid = true
        var error: ValidationError? = null

        if (state.name.isBlank()) {
            error = ValidationError(ValidationErrorType.NAME, "El nombre es obligatorio")
            isValid = false
        } else if (state.description.isBlank() && isValid) {
            error = ValidationError(ValidationErrorType.DESCRIPTION, "La descripción es obligatoria")
            isValid = false
        } else if (state.capital.isBlank() && isValid) {
            error = ValidationError(ValidationErrorType.CAPITAL, "El capital es obligatorio")
            isValid = false
        } else if (state.capital.toDoubleOrNull() == null && isValid) {
            error = ValidationError(ValidationErrorType.CAPITAL, "Ingresa un número válido")
            isValid = false
        } else if ((state.capital.toDoubleOrNull() ?: -1.0) < 0 && isValid) {
            error = ValidationError(ValidationErrorType.CAPITAL, "El capital no puede ser negativo")
            isValid = false
        }

        _uiState.update { it.copy(validationError = error, generalErrorMessage = null) }
        return isValid
    }


    fun createProject() {
        if (!validateInputs()) {
            return
        }

        val currentState = _uiState.value
        val name = currentState.name.trim()
        val description = currentState.description.trim()
        val capitalDouble = currentState.capital.trim().toDouble()

        _uiState.update { it.copy(isLoading = true, generalErrorMessage = null) }

        viewModelScope.launch {
            val result = createProjectUseCase(
                name = name,
                description = description,
                capital = capitalDouble,
                isSelfMade = currentState.isSelfMade
            )

            result.onSuccess { projectId ->
                Log.i("CreateProjectVM", "Proyecto local creado con ID: $projectId")
                _eventFlow.emit(CreateProjectEvent.ShowToast("Proyecto '$name' guardado."))
                _eventFlow.emit(CreateProjectEvent.NavigateBack)
            }.onFailure { exception ->
                Log.e("CreateProjectVM", "Error al crear proyecto local", exception)
                val errorMsg = when (exception) {
                    is ProjectException.NameAlreadyExists -> "El nombre '$name' ya está en uso. Elige otro."
                    else -> exception.message ?: "Error desconocido al guardar."
                }
                _uiState.update { it.copy(generalErrorMessage = errorMsg, validationError = null) }
            }
            _uiState.update { it.copy(isLoading = false) }
        }
    }
}