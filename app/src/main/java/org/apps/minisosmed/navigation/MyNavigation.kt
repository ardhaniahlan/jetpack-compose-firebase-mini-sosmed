package org.apps.minisosmed.navigation

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import org.apps.minisosmed.screen.HomeScreen
import org.apps.minisosmed.screen.LoginScreen
import org.apps.minisosmed.screen.RegisterScreen
import org.apps.minisosmed.viewmodel.AuthViewModel

@Composable
fun MyNavigation(
    authViewModel: AuthViewModel,
    modifier: Modifier,
    snackbarHostState: SnackbarHostState
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "login",
        builder = {
            composable("login"){
                LoginScreen(navController, authViewModel, modifier, snackbarHostState)
            }
            composable("register"){
                RegisterScreen(navController, authViewModel, modifier, snackbarHostState)
            }
            composable("home"){
                HomeScreen(navController)
            }
        }
    )

}