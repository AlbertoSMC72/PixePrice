package com.example.pixelprice.core.ui

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.pixelprice.features.auth.login.presentation.LoginViewModel
import com.example.pixelprice.features.auth.register.presentation.RegisterViewModel
import com.example.pixelprice.features.profile.presentation.ProfileViewModel
import com.example.pixelprice.features.projects.presentation.create.CreateProjectViewModel
import com.example.pixelprice.features.projects.presentation.detail.ProjectDetailViewModel
import com.example.pixelprice.features.projects.presentation.list.ProjectListViewModel
import com.example.pixelprice.MainViewModel

class ProjectListViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProjectListViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProjectListViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class for ProjectListViewModelFactory")
    }
}

class ProjectDetailViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProjectDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProjectDetailViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class for ProjectDetailViewModelFactory")
    }
}

class CreateProjectViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CreateProjectViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CreateProjectViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class for CreateProjectViewModelFactory")
    }
}

class MainViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class for MainViewModelFactory")
    }
}

object BasicViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return try {
            when {
                modelClass.isAssignableFrom(LoginViewModel::class.java) ->
                    @Suppress("UNCHECKED_CAST")
                    LoginViewModel() as T // Constructor sin args

                modelClass.isAssignableFrom(RegisterViewModel::class.java) ->
                    @Suppress("UNCHECKED_CAST")
                    RegisterViewModel() as T // Constructor sin args

                modelClass.isAssignableFrom(ProfileViewModel::class.java) ->
                    @Suppress("UNCHECKED_CAST")
                    ProfileViewModel() as T // Constructor sin args

                // Añadir otros ViewModels simples aquí si los hubiera en el futuro
                else ->
                    // Fallback genérico si no es uno de los anteriores
                    @Suppress("UNCHECKED_CAST")
                    modelClass.getDeclaredConstructor().newInstance() as T
            }
        } catch (e: NoSuchMethodException) {
            throw RuntimeException("Cannot create $modelClass via BasicViewModelFactory. Missing a no-arg constructor or requires a specific factory?", e)
        } catch (e: Exception) {
            throw RuntimeException("Failed to create an instance of $modelClass via BasicViewModelFactory", e)
        }
    }
}

class LoginViewModelFactory() : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LoginViewModel() as T // Login necesita contexto para RegisterDeviceTokenUseCase
        }
        throw IllegalArgumentException("Unknown ViewModel class for LoginViewModelFactory")
    }
}

class RegisterViewModelFactory() : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RegisterViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RegisterViewModel() as T // Register no parece necesitar contexto directamente, pero mantenemos patrón
        }
        throw IllegalArgumentException("Unknown ViewModel class for RegisterViewModelFactory")
    }
}