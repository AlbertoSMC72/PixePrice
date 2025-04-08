package com.example.pixelprice.features.projects.presentation.detail

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
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
import kotlinx.coroutines.Job

data class ProjectDetailUiState(
    val project: ProjectEntity? = null,
    val selectedImageUri: Uri? = null,
    val isLoadingProject: Boolean = true,
    val isRequestingQuote: Boolean = false,
    val isDownloadingQuote: Boolean = false,
    val errorMessage: String? = null,
    val parsedDescription: List<Pair<String, String>> = emptyList()
)

sealed class ProjectDetailEvent {
    data class NavigateToProcessing(val projectId: Int) : ProjectDetailEvent()
    data class ShowToast(val message: String) : ProjectDetailEvent()
    object RequestCameraPermission : ProjectDetailEvent()
    data class RequestGalleryPermission(val permission: String) : ProjectDetailEvent()
    object RequestLegacyWritePermission : ProjectDetailEvent()
    object LaunchGallery : ProjectDetailEvent()
}

class ProjectDetailViewModel(application: Application) : AndroidViewModel(application) {

    private val getProjectByIdUseCase = GetProjectByIdUseCase()
    private val updateProjectImagePathUseCase = UpdateProjectImagePathUseCase()
    private val updateProjectQuotationStatusUseCase = UpdateProjectQuotationStatusUseCase()
    private val requestQuotationUseCase = RequestQuotationUseCase(application)
    private val findQuotationIdByNameUseCase = FindQuotationIdByNameUseCase(application)
    private val downloadQuotationDocxUseCase = DownloadQuotationDocxUseCase(application)

    private val _uiState = MutableStateFlow(ProjectDetailUiState())
    val uiState: StateFlow<ProjectDetailUiState> = _uiState.asStateFlow()

    private val _eventFlow = MutableSharedFlow<ProjectDetailEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    private var currentProjectId: Int = -1
    private var checkStatusJob: Job? = null
    private var quotated: Int = 1

    fun loadProject(projectId: Int, forceReload: Boolean = false) {
        if (!forceReload && projectId == currentProjectId && _uiState.value.project != null && !_uiState.value.isLoadingProject) {
            Log.d("ProjectDetailVM", "Proyecto $projectId ya cargado, no se recarga.")
            return
        }

        currentProjectId = projectId
        _uiState.update { it.copy(isLoadingProject = true, errorMessage = null, project = null, selectedImageUri = null) }
        checkStatusJob?.cancel()

        viewModelScope.launch {
            val result = getProjectByIdUseCase(projectId)
            result.onSuccess { project ->
                val parsedDesc = parseDescription(project.description)
                _uiState.update {
                    it.copy(
                        isLoadingProject = false,
                        project = project,
                        selectedImageUri = null,
                        parsedDescription = parsedDesc
                    )
                }
                Log.i("ProjectDetailVM", "Proyecto $projectId cargado. Pendiente: ${project.hasPendingQuotation}")

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

    private fun checkQuotationStatusIfNeeded(project: ProjectEntity) {
        checkStatusJob = viewModelScope.launch {
            Log.d("ProjectDetailVM", "Verificando estado API para proyecto pendiente: '${project.name}'...")
            val findResult = findQuotationIdByNameUseCase(project.name)

            findResult.onSuccess { quotationId ->
                Log.i("ProjectDetailVM", "Verificación API: Cotización lista para '${project.name}' (ID: $quotationId). Actualizando DB local.")
                val updateResult = updateProjectQuotationStatusUseCase(project.id, quotationId, false)
                if (updateResult.isSuccess) {
                    loadProject(project.id, forceReload = true)
                    _eventFlow.emit(ProjectDetailEvent.ShowToast("¡Cotización lista!"))
                } else {
                    Log.w("ProjectDetailVM", "Fallo al actualizar DB local tras verificación exitosa.")
                }
            }.onFailure { exception ->
                if (exception is QuotationException.NotFound) {
                    Log.d("ProjectDetailVM", "Verificación API: Cotización para '${project.name}' aún no lista (NotFound).")
                    quotated = 0;
                } else {
                    Log.e("ProjectDetailVM", "Error verificando estado API para '${project.name}'", exception)
                    _eventFlow.emit(ProjectDetailEvent.ShowToast("Error al verificar estado online."))
                }
            }
        }
    }

    private fun parseDescription(description: String): List<Pair<String, String>> {
        return description.split('\n')
            .mapNotNull { line ->
                val parts = line.split(":", limit = 2)
                if (parts.size == 2 && parts[0].isNotBlank()) {
                    val label = parts[0].trim()
                    val value = parts[1].trim().ifEmpty { "-" }
                    Pair(label, value)
                } else if (line.isNotBlank()) {
                    Pair("Detalle", line.trim())
                }
                else {
                    null
                }
            }
    }


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


    fun requestCameraPermission() {
        viewModelScope.launch { _eventFlow.emit(ProjectDetailEvent.RequestCameraPermission) }
    }

    fun requestGalleryAccess() {
        viewModelScope.launch {
            val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                Manifest.permission.READ_MEDIA_IMAGES
            } else {
                Manifest.permission.READ_EXTERNAL_STORAGE
            }

            if (hasPermission(permission)) {
                Log.d("ProjectDetailVM", "Permiso de galería concedido, emitiendo evento LaunchGallery.")
                _eventFlow.emit(ProjectDetailEvent.LaunchGallery)
            } else {
                Log.d("ProjectDetailVM", "Permiso de galería no concedido, emitiendo evento RequestGalleryPermission.")
                _eventFlow.emit(ProjectDetailEvent.RequestGalleryPermission(permission))
            }
        }
    }

    private fun hasPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(getApplication(), permission) == PackageManager.PERMISSION_GRANTED
    }

    fun onGalleryPermissionResult(granted: Boolean) {
        viewModelScope.launch {
            if (granted) {
                Log.d("ProjectDetailVM", "Permiso de galería recién concedido, emitiendo evento LaunchGallery.")
                _eventFlow.emit(ProjectDetailEvent.LaunchGallery)
            } else {
                Log.w("ProjectDetailVM", "Permiso de galería denegado por el usuario.")
                _eventFlow.emit(ProjectDetailEvent.ShowToast("Permiso de galería denegado."))
            }
        }
    }

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

    fun onLegacyWritePermissionResult(granted: Boolean) {
        viewModelScope.launch {
            if (granted) {
                _eventFlow.emit(ProjectDetailEvent.ShowToast("Permiso concedido. Intenta descargar de nuevo."))
            } else {
                _eventFlow.emit(ProjectDetailEvent.ShowToast("Permiso denegado. No se puede descargar."))
            }
        }
    }



    fun requestQuotation() {
        viewModelScope.launch{_eventFlow.emit(ProjectDetailEvent.NavigateToProcessing(currentProjectId))}

        val currentProject = _uiState.value.project ?: run {
            viewModelScope.launch{_eventFlow.emit(ProjectDetailEvent.ShowToast("Error: Proyecto no cargado"))}
            return
        }
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
                updateProjectQuotationStatusUseCase(currentProjectId, null, true)
                _eventFlow.emit(ProjectDetailEvent.NavigateToProcessing(currentProjectId))

            }.onFailure { exception ->
                Log.e("ProjectDetailVM", "Error al solicitar cotización", exception)
                val errorMsg = exception.message ?: "Error al solicitar cotización"
                _uiState.update { it.copy(errorMessage = errorMsg) }
                _eventFlow.emit(ProjectDetailEvent.ShowToast(errorMsg))
            }
            _uiState.update { it.copy(isRequestingQuote = false) }
        }
    }

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
            Log.w("ProjectDetailVM", "Intento de descarga fallido: No hay ID de cotización lista para '${project.name}'. Pendiente: ${project.hasPendingQuotation}")
            val errorMsg = if (project.hasPendingQuotation) "La cotización aún está pendiente" else "No hay cotización lista para descargar"
            _uiState.update { it.copy(errorMessage = errorMsg) }
            viewModelScope.launch{ _eventFlow.emit(ProjectDetailEvent.ShowToast(errorMsg)) }
        }
    }
}