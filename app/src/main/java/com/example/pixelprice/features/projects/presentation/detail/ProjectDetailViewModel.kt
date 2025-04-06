package com.example.pixelprice.features.projects.presentation.detail

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build // Necesario para comprobaciones de versión
import android.util.Log
import androidx.core.content.ContextCompat // Necesario para checkSelfPermission
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.pixelprice.features.projects.data.local.ProjectEntity
import com.example.pixelprice.features.projects.domain.usecases.GetProjectByIdUseCase
import com.example.pixelprice.features.projects.domain.usecases.UpdateProjectImagePathUseCase
import com.example.pixelprice.features.projects.domain.usecases.UpdateProjectQuotationStatusUseCase
import com.example.pixelprice.features.quotations.domain.usecases.DownloadQuotationDocxUseCase
import com.example.pixelprice.features.quotations.domain.usecases.FindQuotationIdByNameUseCase
import com.example.pixelprice.features.quotations.domain.usecases.RequestQuotationUseCase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import com.example.pixelprice.features.quotations.data.repository.QuotationException
import kotlinx.coroutines.Job // Importar Job

// Data class y Events (Completos)
data class ProjectDetailUiState(
    val project: ProjectEntity? = null,
    val selectedImageUri: Uri? = null,
    val isLoadingProject: Boolean = true,
    val isRequestingQuote: Boolean = false,
    val isDownloadingQuote: Boolean = false,
    val errorMessage: String? = null
)

sealed class ProjectDetailEvent {
    data class NavigateToProcessing(val projectId: Int) : ProjectDetailEvent()
    data class ShowToast(val message: String) : ProjectDetailEvent()
    object RequestCameraPermission : ProjectDetailEvent()
    data class RequestGalleryPermission(val permission: String) : ProjectDetailEvent()
    object RequestLegacyWritePermission : ProjectDetailEvent()
}

class ProjectDetailViewModel(application: Application) : AndroidViewModel(application) {

    // Instancias de los Casos de Uso
    private val getProjectByIdUseCase = GetProjectByIdUseCase()
    private val updateProjectImagePathUseCase = UpdateProjectImagePathUseCase()
    private val updateProjectQuotationStatusUseCase = UpdateProjectQuotationStatusUseCase()
    private val requestQuotationUseCase = RequestQuotationUseCase(application) // Necesita contexto
    private val findQuotationIdByNameUseCase = FindQuotationIdByNameUseCase(application) // Necesita contexto
    private val downloadQuotationDocxUseCase = DownloadQuotationDocxUseCase(application) // Necesita contexto

    // StateFlow para el estado de la UI
    private val _uiState = MutableStateFlow(ProjectDetailUiState())
    val uiState: StateFlow<ProjectDetailUiState> = _uiState.asStateFlow()

    // SharedFlow para eventos
    private val _eventFlow = MutableSharedFlow<ProjectDetailEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    private var currentProjectId: Int = -1
    private var checkStatusJob: Job? = null // Referencia al job de verificación de estado
    private var quotated: Int = 1

    /**
     * Carga los detalles del proyecto desde la base de datos local.
     * Si el proyecto está marcado como pendiente, inicia una verificación
     * en segundo plano contra la API para ver si la cotización ya está lista.
     *
     * @param projectId ID del proyecto a cargar.
     * @param forceReload Si es true, fuerza la recarga aunque el ID sea el mismo. Útil después de actualizar estado.
     */
    fun loadProject(projectId: Int, forceReload: Boolean = false) {
        // Evita recargas innecesarias a menos que se fuerce
        if (!forceReload && projectId == currentProjectId && _uiState.value.project != null && !_uiState.value.isLoadingProject) {
            Log.d("ProjectDetailVM", "Proyecto $projectId ya cargado, no se recarga.")
            return
        }

        currentProjectId = projectId
        _uiState.update { it.copy(isLoadingProject = true, errorMessage = null, project = null, selectedImageUri = null) }
        checkStatusJob?.cancel() // Cancelar verificación anterior si existía

        viewModelScope.launch {
            val result = getProjectByIdUseCase(projectId)
            result.onSuccess { project ->
                _uiState.update {
                    it.copy(isLoadingProject = false, project = project, selectedImageUri = null)
                }
                Log.i("ProjectDetailVM", "Proyecto $projectId cargado. Pendiente: ${project.hasPendingQuotation}")

                // Si el proyecto está pendiente, verificar si ya está listo en el backend
                if (project.hasPendingQuotation) {
                    checkQuotationStatusIfNeeded(project)
                }
            }.onFailure { exception ->
                Log.e("ProjectDetailVM", "Error cargando proyecto $projectId", exception)
                _uiState.update {
                    it.copy(isLoadingProject = false, errorMessage = exception.message ?: "Error al cargar el proyecto.")
                }
            }
        }
    }

    /**
     * Verifica con la API si la cotización para un proyecto pendiente ya está lista.
     * Si está lista, actualiza el estado en la base de datos local.
     * Se ejecuta en una corutina separada.
     */
    private fun checkQuotationStatusIfNeeded(project: ProjectEntity) {
        // Evitar lanzar múltiples verificaciones si ya hay una en curso
        //if (_uiState.value.isCheckingStatus) return
        //_uiState.update { it.copy(isCheckingStatus = true) } // Marcar inicio de verificación (opcional para UI)
        checkStatusJob = viewModelScope.launch {
            Log.d("ProjectDetailVM", "Verificando estado API para proyecto pendiente: '${project.name}'...")
            val findResult = findQuotationIdByNameUseCase(project.name)

            findResult.onSuccess { quotationId ->
                Log.i("ProjectDetailVM", "Verificación API: Cotización lista para '${project.name}' (ID: $quotationId). Actualizando DB local.")
                // Cotización encontrada, actualizar estado local
                val updateResult = updateProjectQuotationStatusUseCase(project.id, quotationId, false) // isPending = false
                if (updateResult.isSuccess) {
                    loadProject(project.id, forceReload = true) // Recargar para reflejar cambio en UI
                    _eventFlow.emit(ProjectDetailEvent.ShowToast("¡Cotización lista!"))
                } else {
                    Log.w("ProjectDetailVM", "Fallo al actualizar DB local tras verificación exitosa.")
                    // No mostrar error al usuario, es un fallo interno
                }
            }.onFailure { exception ->
                if (exception is QuotationException.NotFound) {
                    Log.d("ProjectDetailVM", "Verificación API: Cotización para '${project.name}' aún no lista (NotFound).")
                    quotated = 0;
                } else {
                    Log.e("ProjectDetailVM", "Error verificando estado API para '${project.name}'", exception)
                    // Hubo un error de red o servidor al verificar
                    _eventFlow.emit(ProjectDetailEvent.ShowToast("Error al verificar estado online."))
                }
            }
            //_uiState.update { it.copy(isCheckingStatus = false) } // Marcar fin de verificación
        }
    }


    /**
     * Actualiza el estado de la UI con el Uri de la imagen seleccionada
     * y guarda el Uri (como String) en la base de datos local.
     */
    fun onImageSelected(uri: Uri) {
        _uiState.update { it.copy(selectedImageUri = uri, errorMessage = null) }
        viewModelScope.launch {
            val result = updateProjectImagePathUseCase(currentProjectId, uri.toString())
            result.onSuccess { Log.d("ProjectDetailVM", "URI de imagen guardado en DB para proyecto $currentProjectId") }
                .onFailure { exception ->
                    Log.e("ProjectDetailVM", "Error al guardar URI de imagen en DB", exception)
                    _eventFlow.emit(ProjectDetailEvent.ShowToast("Error al guardar imagen"))
                }
        }
    }

    // --- Lógica de Permisos ---

    /** Emite evento para solicitar permiso de cámara. */
    fun requestCameraPermission() {
        viewModelScope.launch { _eventFlow.emit(ProjectDetailEvent.RequestCameraPermission) }
    }

    /** Comprueba si tiene permiso de galería y lo solicita si no lo tiene. */
    fun requestGalleryAccess() {
        viewModelScope.launch {
            val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                Manifest.permission.READ_MEDIA_IMAGES
            } else {
                Manifest.permission.READ_EXTERNAL_STORAGE
            }
            if (hasPermission(permission)) {
                // Si tiene permiso, la UI lanzará el selector. Mostrar Toast opcional.
                _eventFlow.emit(ProjectDetailEvent.ShowToast("Abriendo galería..."))
            } else {
                _eventFlow.emit(ProjectDetailEvent.RequestGalleryPermission(permission))
            }
        }
    }

    /** Comprueba si necesita permiso de escritura legado y lo solicita si es necesario. Retorna false si se necesita y no se tiene. */
    private fun requestLegacyWriteAccessIfNeeded(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            val permission = Manifest.permission.WRITE_EXTERNAL_STORAGE
            if (!hasPermission(permission)) {
                viewModelScope.launch { _eventFlow.emit(ProjectDetailEvent.RequestLegacyWritePermission) }
                return false
            }
        }
        return true
    }

    /** Helper interno para verificar permisos. */
    private fun hasPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(getApplication(), permission) == PackageManager.PERMISSION_GRANTED
    }

    /** Maneja el resultado del permiso de escritura legado. */
    fun onLegacyWritePermissionResult(granted: Boolean) {
        viewModelScope.launch {
            if (granted) {
                _eventFlow.emit(ProjectDetailEvent.ShowToast("Permiso concedido. Intenta descargar de nuevo."))
            } else {
                _eventFlow.emit(ProjectDetailEvent.ShowToast("Permiso denegado. No se puede descargar."))
            }
        }
    }

    /** Maneja el resultado del permiso de galería (Opcional). */
    fun onGalleryPermissionResult(granted: Boolean) {
        // La UI ya lanza el selector si granted es true.
        if (!granted) {
            viewModelScope.launch { _eventFlow.emit(ProjectDetailEvent.ShowToast("Permiso de galería denegado.")) }
        }
    }


    // --- Lógica de Acciones Principales ---

    /**
     * Inicia la solicitud de cotización a la API,
     * usando los datos del proyecto actual y la imagen seleccionada/guardada.
     * Actualiza el estado local a pendiente y navega a la pantalla de procesamiento.
     */
    fun requestQuotation() {
        val currentProject = _uiState.value.project ?: run {
            viewModelScope.launch{_eventFlow.emit(ProjectDetailEvent.ShowToast("Error: Proyecto no cargado"))}
            return
        }
        // Usar imagen seleccionada si existe, si no, la guardada en DB
        val imageUriToUpload = _uiState.value.selectedImageUri ?: currentProject.imageUri?.let { Uri.parse(it) }

        _uiState.update { it.copy(isRequestingQuote = true, errorMessage = null) }
        viewModelScope.launch {
            val result = requestQuotationUseCase(
                projectName = currentProject.name,
                projectDescription = currentProject.description,
                projectCapital = currentProject.capital,
                projectIsSelfMade = currentProject.isSelfMade,
                mockupImageUri = imageUriToUpload
            )
            result.onSuccess {
                Log.i("ProjectDetailVM", "Solicitud de cotización aceptada para proyecto ${currentProject.id}")
                // Marcar localmente como pendiente y limpiar ID anterior
                updateProjectQuotationStatusUseCase(currentProjectId, null, true)
                // Navegar a pantalla de espera
                _eventFlow.emit(ProjectDetailEvent.NavigateToProcessing(currentProjectId))
                // Recargar datos al volver para reflejar estado pendiente
                // (Se hará automáticamente al entrar de nuevo o forzado desde checkStatusIfNeeded)
                // loadProject(currentProjectId) // No es estrictamente necesario aquí

            }.onFailure { exception ->
                Log.e("ProjectDetailVM", "Error al solicitar cotización", exception)
                val errorMsg = exception.message ?: "Error al solicitar cotización"
                _uiState.update { it.copy(errorMessage = errorMsg) }
                _eventFlow.emit(ProjectDetailEvent.ShowToast(errorMsg))
            }
            _uiState.update { it.copy(isRequestingQuote = false) }
        }
    }

    /**
     * Inicia la descarga del reporte DOCX de la cotización asociada al proyecto actual.
     * Obtiene el ID de la cotización desde el estado local del proyecto.
     * Maneja la solicitud de permiso de escritura si es necesario (< API 29).
     */
    fun downloadQuotationReport() {
        if (!requestLegacyWriteAccessIfNeeded()) {
            Log.w("ProjectDetailVM", "Descarga detenida: Permiso de escritura necesario y no concedido.")
            return
        }

        val project = _uiState.value.project ?: run {
            viewModelScope.launch{_eventFlow.emit(ProjectDetailEvent.ShowToast("Error: Proyecto no cargado"))}
            return
        }

        val quotationIdToDownload = project.lastQuotationId

        //if (quotationIdToDownload != null && quotationIdToDownload > 0) {
        if (quotated != 0) {
            _uiState.update { it.copy(isDownloadingQuote = true, errorMessage = null) }
            viewModelScope.launch {
                Log.d("ProjectDetailVM", "Iniciando descarga para Cotización ID: $quotationIdToDownload (Proyecto: ${project.name})")
                val downloadResult = downloadQuotationDocxUseCase(project.name)
                downloadResult.onSuccess {
                    Log.i("ProjectDetailVM", "Descarga encolada para cotización $quotationIdToDownload.")
                    _eventFlow.emit(ProjectDetailEvent.ShowToast("Descarga iniciada: ${project.name}.docx"))
                }.onFailure { exception ->
                    Log.e("ProjectDetailVM", "Error al descargar cotización $quotationIdToDownload", exception)
                    val errorMsg = exception.message ?: "Error al descargar reporte"
                    _uiState.update { it.copy(errorMessage = errorMsg) }
                    _eventFlow.emit(ProjectDetailEvent.ShowToast("Error descarga: $errorMsg"))
                }
                _uiState.update { it.copy(isDownloadingQuote = false) }
            }
        } else {
            // No hay ID o el proyecto está pendiente
            Log.w("ProjectDetailVM", "Intento de descarga fallido: No hay ID de cotización lista para '${project.name}'. Pendiente: ${project.hasPendingQuotation}")
            val errorMsg = if (project.hasPendingQuotation) "La cotización aún está pendiente" else "No hay cotización lista para descargar"
            _uiState.update { it.copy(errorMessage = errorMsg) }
            viewModelScope.launch{ _eventFlow.emit(ProjectDetailEvent.ShowToast(errorMsg)) }
        }
    }
}