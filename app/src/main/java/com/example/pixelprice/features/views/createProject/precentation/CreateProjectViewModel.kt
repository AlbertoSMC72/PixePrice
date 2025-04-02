package com.example.pixelprice.features.views.createProject.precentation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pixelprice.features.views.createProject.data.model.CreateProjectRequest
import com.example.pixelprice.features.views.createProject.domain.CreateProjectUseCase
import kotlinx.coroutines.launch

class CreateProjectViewModel : ViewModel() {

    private val createProjectUseCase = CreateProjectUseCase()

    private val _name = MutableLiveData("")
    val name: LiveData<String> = _name

    private val _description = MutableLiveData("")
    val description: LiveData<String> = _description

    private val _capital = MutableLiveData("")
    val capital: LiveData<String> = _capital

    private val _isSelfMade = MutableLiveData(false)
    val isSelfMade: LiveData<Boolean> = _isSelfMade

    private val _error = MutableLiveData("")
    val error: LiveData<String> = _error

    private val _success = MutableLiveData(false)
    val success: LiveData<Boolean> = _success

    fun onNameChanged(value: String) {
        _name.value = value
    }

    fun onDescriptionChanged(value: String) {
        _description.value = value
    }

    fun onCapitalChanged(value: String) {
        _capital.value = value
    }

    fun onSelfMadeChanged(value: Boolean) {
        _isSelfMade.value = value
    }

    fun onCreateProject() {
        val nameValue = _name.value.orEmpty()
        val descValue = _description.value.orEmpty()
        val capitalValue = _capital.value.orEmpty().toDoubleOrNull()
        val isSelfMadeValue = _isSelfMade.value ?: false
        val userId = 1 // TODO: Obtener de SharedPreferences

        if (nameValue.isBlank() || descValue.isBlank() || capitalValue == null) {
            _error.value = "Por favor, completa todos los campos correctamente"
            return
        }

        val request = CreateProjectRequest(
            userId = userId,
            name = nameValue,
            description = descValue,
            capital = capitalValue,
            isSelfMade = isSelfMadeValue
        )

        viewModelScope.launch {
            val result = createProjectUseCase(request)
            result.onSuccess {
                _success.value = true
                _error.value = ""
            }.onFailure {
                _error.value = it.message ?: "Error al crear el proyecto"
                _success.value = false
            }
        }
    }
}
