package com.example.pixelprice.features.views.home.precentation


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pixelprice.features.views.home.data.model.ProjectDTO
import kotlinx.coroutines.launch
import com.example.pixelprice.features.views.home.data.repository.HomeRepository
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData


class HomeViewModel : ViewModel() {

    private val repository = HomeRepository()

    private val _projects = MutableLiveData<List<ProjectDTO>>(emptyList())
    val projects: LiveData<List<ProjectDTO>> = _projects

    private val _error = MutableLiveData<String>("")
    val error: LiveData<String> = _error

    fun loadProjects(userId: Int = 1) {
        viewModelScope.launch {
            val result = repository.getUserProjects(userId)
            result.onSuccess {
                _projects.value = it
            }.onFailure {
                _error.value = it.message ?: "Error al cargar proyectos"
            }
        }
    }
}
