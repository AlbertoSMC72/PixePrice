package com.example.pixelprice.core.navigation

import android.app.Application
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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

@Composable
fun NavigationWrapper(navController: NavHostController) {

    val context = LocalContext.current
    val application = context.applicationContext as Application

    // --- Instanciar Factories Necesarias ---
    // Ya NO se necesitan LoginViewModelFactory ni RegisterViewModelFactory
    val projectListVMFactory = remember { ProjectListViewModelFactory(application) }
    val createProjectVMFactory = remember { CreateProjectViewModelFactory(application) }
    val projectDetailVMFactory = remember { ProjectDetailViewModelFactory(application) }
    // BasicViewModelFactory es un object, no necesita remember

    // Opcional: Log ruta actual
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    LaunchedEffect(currentRoute) {
        Log.d("NavigationWrapper", "Ruta actual: $currentRoute")
    }

    NavHost(navController = navController, startDestination = Routes.LOGIN) {

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

        // --- Profile Screen ---
        composable(route = Routes.PROFILE) {
            val userId = UserInfoProvider.userID
            if (userId == 0) { /* ... redirect to login ... */ return@composable }

            // *** CORREGIDO: Usar BasicViewModelFactory ***
            val viewModel: ProfileViewModel = viewModel(factory = BasicViewModelFactory)
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
    } // Fin NavHost
}