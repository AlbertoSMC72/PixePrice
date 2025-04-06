package com.example.pixelprice.core.navigation

import android.app.Application
import android.util.Log
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.example.pixelprice.core.data.UserInfoProvider
import com.example.pixelprice.core.ui.*
import com.example.pixelprice.features.auth.login.presentation.LoginScreen
import com.example.pixelprice.features.auth.login.presentation.LoginViewModel
import com.example.pixelprice.features.auth.register.presentation.RegisterScreen
import com.example.pixelprice.features.auth.register.presentation.RegisterViewModel
import com.example.pixelprice.features.profile.presentation.ProfileScreen
import com.example.pixelprice.features.profile.presentation.ProfileViewModel
import com.example.pixelprice.features.projects.presentation.create.CreateProjectScreen
import com.example.pixelprice.features.projects.presentation.create.CreateProjectViewModel
import com.example.pixelprice.features.projects.presentation.detail.ProjectDetailScreen
import com.example.pixelprice.features.projects.presentation.detail.ProjectDetailViewModel
import com.example.pixelprice.features.projects.presentation.list.ProjectListScreen
import com.example.pixelprice.features.projects.presentation.list.ProjectListViewModel
import com.example.pixelprice.features.quotations.presentation.processing.ProcessingScreen
import com.example.pixelprice.core.data.TokenManager

@Composable
fun NavigationWrapper(navController: NavHostController) {

    val context = LocalContext.current
    val application = context.applicationContext as Application

    val projectListVMFactory = remember { ProjectListViewModelFactory(application) }
    val createProjectVMFactory = remember { CreateProjectViewModelFactory(application) }
    val projectDetailVMFactory = remember { ProjectDetailViewModelFactory(application) }
    val profileVMFactory = remember { ProfileViewModelFactory(application) }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    LaunchedEffect(currentRoute) {
        Log.d("NavigationWrapper", "Ruta actual: $currentRoute")
    }

    val startDestination = remember {
        if (TokenManager.isSessionActive()) {
            // Si hay sesión activa, cargar info del usuario en el provider en memoria
            // (Necesario si otros componentes leen directamente de UserInfoProvider)
            val userId = TokenManager.getUserId()
            val username = TokenManager.getUsername()
            if (userId != 0) { // Asegurarse que realmente hay un ID guardado
                UserInfoProvider.setUserInfo(userId, username)
                Log.d("NavigationWrapper", "Sesión activa encontrada para User ID: $userId. Iniciando en ProjectList.")
                Routes.PROJECT_LIST // Ir a la lista de proyectos
            } else {
                // Algo raro pasó (token activo pero sin ID?), mejor ir a Login
                Log.w("NavigationWrapper", "Token activo pero User ID no encontrado en SharedPreferences. Iniciando en Login.")
                TokenManager.clearToken() // Limpiar estado inconsistente
                Routes.LOGIN
            }
        } else {
            Log.d("NavigationWrapper", "No hay sesión activa. Iniciando en Login.")
            Routes.LOGIN // Ir a la pantalla de login
        }
    }

    NavHost(navController = navController, startDestination = startDestination) {

        // --- Auth Screens ---
        composable(route = Routes.LOGIN) {
            // *** CORREGIDO: Usar BasicViewModelFactory ***
            val viewModel: LoginViewModel = viewModel(factory = BasicViewModelFactory)
            LoginScreen(loginViewModel = viewModel) { destination ->
                navController.navigate(destination) {
                    popUpTo(navController.graph.startDestinationId) { inclusive = true }
                    launchSingleTop = true
                }
            }
        }

        composable(route = Routes.REGISTER) {
            // *** CORREGIDO: Usar BasicViewModelFactory ***
            val viewModel: RegisterViewModel = viewModel(factory = BasicViewModelFactory)
            RegisterScreen(registerViewModel = viewModel) { destination ->
                navController.navigate(destination) {
                    popUpTo(Routes.LOGIN) { inclusive = true }
                    launchSingleTop = true
                }
            }
        }

        // --- Project Screens (Local) ---
        composable(route = Routes.PROJECT_LIST) {
            val viewModel: ProjectListViewModel = viewModel(factory = projectListVMFactory)
            ProjectListScreen(
                viewModel = viewModel,
                onNavigateToCreate = { navController.navigate(Routes.CREATE_PROJECT) },
                onNavigateToDetail = { projectId -> navController.navigate(Routes.projectDetail(projectId)) },
                onNavigateToProfile = { navController.navigate(Routes.PROFILE) },
            )
        }

        composable(route = Routes.CREATE_PROJECT) {
            val viewModel: CreateProjectViewModel = viewModel(factory = createProjectVMFactory)
            CreateProjectScreen(
                viewModel = viewModel,
                onProjectCreated = { navController.popBackStack() }
            )
        }

        composable(
            route = Routes.PROJECT_DETAIL_ROUTE,
            arguments = listOf(navArgument(Routes.ARG_PROJECT_ID) { type = NavType.IntType })
        ) { backStackEntry ->
            val projectId = backStackEntry.arguments?.getInt(Routes.ARG_PROJECT_ID) ?: -1
            if (projectId == -1) { /* ... error handling ... */ return@composable }

            val viewModel: ProjectDetailViewModel = viewModel(factory = projectDetailVMFactory)

            ProjectDetailScreen(
                viewModel = viewModel,
                projectId = projectId,
                onNavigateToProcessing = { pId -> navController.navigate(Routes.processing(pId)) },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // --- Processing Screen ---
        composable(
            route = Routes.PROCESSING_ROUTE,
            arguments = listOf(navArgument(Routes.ARG_PROJECT_ID) { type = NavType.IntType })
        ) { backStackEntry ->
            val projectId = backStackEntry.arguments?.getInt(Routes.ARG_PROJECT_ID) ?: -1
            if (projectId == -1) { /* ... error handling ... */ return@composable }
            ProcessingScreen(
                projectId = projectId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(route = Routes.PROFILE) {
            val userId = TokenManager.getUserId() // Más seguro obtenerlo directamente
            if (userId == 0) {
                // Redirigir a Login si no hay ID
                LaunchedEffect(Unit) {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(navController.graph.id) { inclusive = true }
                    }
                }
                Spacer(Modifier.fillMaxSize()) // Mostrar algo mientras redirige
                return@composable
            }

            val viewModel: ProfileViewModel = viewModel(factory = profileVMFactory)

            ProfileScreen(
                viewModel = viewModel,
                userId = userId,
                onLogout = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(navController.graph.id) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }
    }
}