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
import kotlinx.coroutines.flow.catch // *** IMPORTAR ***
import kotlinx.coroutines.flow.map // *** IMPORTAR ***
import kotlinx.coroutines.flow.onStart // *** IMPORTAR ***
import kotlinx.coroutines.flow.stateIn

data class ProjectListUiState(
    val projects: List<ProjectEntity> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isRefreshing: Boolean = false
)

class ProjectListViewModel(application: Application) : AndroidViewModel(application) {

    private val getUserProjectsUseCase = GetUserProjectsUseCase() // Repositorio accede a DB local
    private val findQuotationIdByNameUseCase = FindQuotationIdByNameUseCase(application)
    private val updateProjectQuotationStatusUseCase = UpdateProjectQuotationStatusUseCase()

    private val _uiState = MutableStateFlow(ProjectListUiState())
    val uiState: StateFlow<ProjectListUiState> = getUserProjectsUseCase() // Llama al use case que devuelve Flow
        .onStart {
            // Opcional: Emitir estado de carga inicial, aunque puede ser muy rápido
            Log.d("ProjectListVM", "Iniciando flujo de proyectos...")
            // No podemos emitir directamente aquí si usamos map/catch/stateIn así.
            // El estado inicial de isLoading se controla en el valor inicial de stateIn.
        }
        .map { projects ->
            // Mapea la lista de proyectos a un estado de éxito
            Log.d("ProjectListVM", "Proyectos recibidos del Flow: ${projects.size}")
            ProjectListUiState(projects = projects, isLoading = false, errorMessage = null)
        }
        .catch { exception ->
            // Atrapa errores del Flow (ej. NotAuthenticated del UseCase o error de DB)
            Log.e("ProjectListVM", "Error en el Flow de proyectos", exception)
            // Emite un estado de error
            emit(ProjectListUiState(isLoading = false, errorMessage = mapError(exception)))
        }
        .stateIn(
            scope = viewModelScope, // El scope en el que vivirá el StateFlow
            // Mantener el Flow activo 5 segundos después de que la UI deje de observar.
            // Evita reiniciar el Flow en cambios rápidos (rotación, etc.)
            started = SharingStarted.WhileSubscribed(5000L),
            // Estado inicial mientras el Flow se suscribe por primera vez
            initialValue = ProjectListUiState(isLoading = true)
        )

    private fun mapError(exception: Throwable): String {
        return when (exception) {
            is ProjectException.NotAuthenticated -> "Inicia sesión para ver tus proyectos."
            is ProjectException.DatabaseError -> "Error al acceder a los datos locales."
            // Añadir otros mapeos si es necesario
            else -> exception.message ?: "Error desconocido al cargar proyectos."
        }
    }
}
