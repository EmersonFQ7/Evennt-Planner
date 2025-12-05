package com.tecsup.eventplanner_flores.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.tecsup.eventplanner_flores.data.repository.AuthRepository
import com.tecsup.eventplanner_flores.ui.screens.create.CreateEventScreen
import com.tecsup.eventplanner_flores.ui.screens.edit.EditEventScreen
import com.tecsup.eventplanner_flores.ui.screens.events.EventListScreen
import com.tecsup.eventplanner_flores.ui.screens.login.LoginScreen
import com.tecsup.eventplanner_flores.ui.screens.register.RegisterScreen

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object EventList : Screen("event_list")
    object CreateEvent : Screen("create_event")
    object EditEvent : Screen("edit_event/{eventId}") {
        fun createRoute(eventId: String) = "edit_event/$eventId"
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val authRepository = AuthRepository()

    val startDestination = if (authRepository.isUserLoggedIn()) {
        Screen.EventList.route
    } else {
        Screen.Login.route
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                },
                onLoginSuccess = {
                    navController.navigate(Screen.EventList.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                onNavigateToLogin = {
                    navController.popBackStack()
                },
                onRegisterSuccess = {
                    navController.navigate(Screen.EventList.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.EventList.route) {
            // Verificar autenticación al entrar a esta pantalla
            LaunchedEffect(Unit) {
                if (!authRepository.isUserLoggedIn()) {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            }

            EventListScreen(
                onNavigateToCreate = {
                    navController.navigate(Screen.CreateEvent.route)
                },
                onNavigateToEdit = { eventId ->
                    navController.navigate(Screen.EditEvent.createRoute(eventId))
                },
                onLogout = {
                    // Navegar primero, luego hacer logout
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(Screen.CreateEvent.route) {
            CreateEventScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Screen.EditEvent.route,
            arguments = listOf(
                navArgument("eventId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getString("eventId") ?: ""
            EditEventScreen(
                eventId = eventId,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}