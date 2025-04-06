package com.example.pixelprice.features.projects.presentation.list

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.pixelprice.features.projects.data.local.ProjectEntity
import com.example.pixelprice.features.projects.domain.usecases.GetUserProjectsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProjectListUiState(
    val projects: List<ProjectEntity> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class ProjectListViewModel(application: Application) : AndroidViewModel(application) {

    private val getUserProjectsUseCase = GetUserProjectsUseCase() // Repositorio accede a DB local

    private val _uiState = MutableStateFlow(ProjectListUiState())
    val uiState: StateFlow<ProjectListUiState> = _uiState.asStateFlow()

    init {
        loadProjects()
    }

    fun loadProjects() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            val result = getUserProjectsUseCase()
            result.onSuccess { projects ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        projects = projects
                    )
                }
                Log.i("ProjectListViewModel", "Proyectos locales cargados: ${projects.size}")
            }.onFailure { exception ->
                Log.e("ProjectListViewModel", "Error al cargar proyectos locales", exception)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = exception.message ?: "Error al cargar proyectos locales."
                    )
                }
            }
        }
    }
}