package com.example.pixelprice.features.projects.presentation.list

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.pixelprice.features.projects.data.local.ProjectEntity
import com.example.pixelprice.features.projects.data.repository.ProjectException
import com.example.pixelprice.features.projects.domain.usecases.GetUserProjectsUseCase
import com.example.pixelprice.features.projects.domain.usecases.UpdateProjectQuotationStatusUseCase
import com.example.pixelprice.features.quotations.domain.usecases.FindQuotationIdByNameUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn

data class ProjectListUiState(
    val projects: List<ProjectEntity> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isRefreshing: Boolean = false
)

class ProjectListViewModel(application: Application) : AndroidViewModel(application) {

    private val getUserProjectsUseCase = GetUserProjectsUseCase()
    private val findQuotationIdByNameUseCase = FindQuotationIdByNameUseCase(application)
    private val updateProjectQuotationStatusUseCase = UpdateProjectQuotationStatusUseCase()

    private val _uiState = MutableStateFlow(ProjectListUiState())
    val uiState: StateFlow<ProjectListUiState> = getUserProjectsUseCase()
        .onStart {
            Log.d("ProjectListVM", "Iniciando flujo de proyectos...")
        }
        .map { projects ->
            Log.d("ProjectListVM", "Proyectos recibidos del Flow: ${projects.size}")
            ProjectListUiState(projects = projects, isLoading = false, errorMessage = null)
        }
        .catch { exception ->
            Log.e("ProjectListVM", "Error en el Flow de proyectos", exception)
            emit(ProjectListUiState(isLoading = false, errorMessage = mapError(exception)))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = ProjectListUiState(isLoading = true)
        )

    private fun mapError(exception: Throwable): String {
        return when (exception) {
            is ProjectException.NotAuthenticated -> "Inicia sesión para ver tus proyectos."
            is ProjectException.DatabaseError -> "Error al acceder a los datos locales."
            else -> exception.message ?: "Error desconocido al cargar proyectos."
        }
    }
}
