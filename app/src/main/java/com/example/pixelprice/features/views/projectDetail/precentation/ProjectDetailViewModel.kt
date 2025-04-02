package com.example.pixelprice.features.views.projectDetail.precentation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pixelprice.features.views.createProject.data.model.ProjectDTO
import com.example.pixelprice.features.views.processing.domain.RequestQuoteUseCase
import com.example.pixelprice.features.views.projectDetail.domain.GetProjectUseCase
import kotlinx.coroutines.launch

class ProjectDetailViewModel : ViewModel() {

    private val getProjectUseCase = GetProjectUseCase()
    private val requestQuoteUseCase = RequestQuoteUseCase()

    private val _project = MutableLiveData<ProjectDTO?>()
    val project: LiveData<ProjectDTO?> = _project

    private val _error = MutableLiveData<String>("")
    val error: LiveData<String> = _error

    private val _quoteRequested = MutableLiveData<Boolean>(false)
    val quoteRequested: LiveData<Boolean> = _quoteRequested

    fun loadProject(id: Int) {
        viewModelScope.launch {
            val result = getProjectUseCase(id)
            result.onSuccess {
                _project.value = it
            }.onFailure {
                _error.value = it.message ?: "Error al obtener proyecto"
            }
        }
    }

    fun requestQuote(id: Int) {
        viewModelScope.launch {
            val result = requestQuoteUseCase(id)
            result.onSuccess {
                _quoteRequested.value = true
            }.onFailure {
                _error.value = it.message ?: "No se pudo solicitar cotización"
            }
        }
    }
}
