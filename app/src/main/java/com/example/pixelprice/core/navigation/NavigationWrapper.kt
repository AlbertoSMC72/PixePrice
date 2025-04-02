package com.example.pixelprice.core.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.pixelprice.features.authorization.login.presentation.LoginScreen
import com.example.pixelprice.features.authorization.login.presentation.LoginViewModel
import com.example.pixelprice.features.authorization.register.presentation.RegisterScreen
import com.example.pixelprice.features.authorization.register.presentation.RegisterViewModel
import com.example.pixelprice.core.data.UserInfoProvider
import com.example.pixelprice.features.views.home.precentation.HomeScreen
import com.example.pixelprice.features.views.home.precentation.HomeViewModel
import com.example.pixelprice.features.views.createProject.precentation.CreateProjectScreen
import com.example.pixelprice.features.views.createProject.precentation.CreateProjectViewModel
import com.example.pixelprice.features.views.profile.precentation.ProfileScreen
import com.example.pixelprice.features.views.profile.precentation.ProfileViewModel
import com.example.pixelprice.features.views.projectDetail.precentation.ProjectDetailScreen
import com.example.pixelprice.features.views.projectDetail.precentation.ProjectDetailViewModel
import com.example.pixelprice.features.views.quoteResult.precentation.QuoteResultScreen
import com.example.pixelprice.features.views.quoteResult.precentation.QuoteViewModel
import com.example.pixelprice.features.views.processing.ProcessingScreen
import androidx.navigation.NavType
import androidx.navigation.navArgument



@Composable
fun NavigationWrapper() {
    val navController = rememberNavController()
    val userId = UserInfoProvider.userID

    NavHost(navController = navController, startDestination = "Login") {

        composable("Login") {
            LoginScreen(LoginViewModel()) { destination ->
                navController.navigate(destination)
            }
        }
        composable("Register") {
            RegisterScreen(RegisterViewModel()) { destination ->
                navController.navigate(destination)
            }
        }
        composable("Home") {
            HomeScreen(
                viewModel = HomeViewModel(),
                onNavigateToCreate = { navController.navigate("CreateProject") },
                onNavigateToDetail = { projectId -> navController.navigate("ProjectDetail/$projectId") }
            )
        }

        composable("CreateProject") {
            CreateProjectScreen(
                viewModel = CreateProjectViewModel(),
                onProjectCreated = { navController.navigate("Home") }
            )
        }

        composable(
            route = "ProjectDetail/{id}",
            arguments = listOf(navArgument("id") { type = NavType.IntType })
        ) { backStackEntry ->
            val projectId = backStackEntry.arguments?.getInt("id") ?: return@composable
            ProjectDetailScreen(
                viewModel = ProjectDetailViewModel(),
                projectId = projectId,
                onNavigateToQuote = { navController.navigate("QuoteResult/$projectId") }
            )
        }

        composable(
            route = "QuoteResult/{id}",
            arguments = listOf(navArgument("id") { type = NavType.IntType })
        ) { backStackEntry ->
            val projectId = backStackEntry.arguments?.getInt("id") ?: return@composable
            QuoteResultScreen(
                viewModel = QuoteViewModel(),
                projectId = projectId,
                onBack = { navController.popBackStack("Home", false) }
            )
        }


        composable("Profile") {
            ProfileScreen(
                viewModel = ProfileViewModel(),
                userId = userId
            )
        }

        composable(
            route = "Processing/{id}",
            arguments = listOf(navArgument("id") { type = NavType.IntType })
        ) { backStackEntry ->
            val projectId = backStackEntry.arguments?.getInt("id") ?: return@composable
            ProcessingScreen(
                projectId = projectId,
                onNavigateToQuote = {
                    navController.navigate("QuoteResult/$projectId") {
                        popUpTo("Processing/$projectId") { inclusive = true }
                    }
                }
            )
        }

    }
}