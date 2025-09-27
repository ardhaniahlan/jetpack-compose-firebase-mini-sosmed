package org.apps.minisosmed.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import org.apps.minisosmed.screen.AddPostScreen
import org.apps.minisosmed.screen.HomeScreen
import org.apps.minisosmed.screen.LoginScreen
import org.apps.minisosmed.screen.ProfileScreen
import org.apps.minisosmed.screen.RegisterScreen
import org.apps.minisosmed.screen.SearchScreen
import org.apps.minisosmed.screen.SplashScreen
import org.apps.minisosmed.viewmodel.AuthViewModel

@Composable
fun MyNavigation(
    authViewModel: AuthViewModel,
    modifier: Modifier,
    snackbarHostState: SnackbarHostState,
    navController: NavHostController
) {
    NavHost(
        navController = navController,
        startDestination = "splash",
        builder = {
            composable("login"){
                LoginScreen(navController, authViewModel, modifier, snackbarHostState)
            }
            composable("register"){
                RegisterScreen(navController, authViewModel, modifier, snackbarHostState)
            }
            composable("splash"){
                SplashScreen(navController)
            }

            // Bottom Bar
            composable("home"){
                HomeScreen(navController)
            }
            composable("addpost"){
                AddPostScreen(navController)
            }
            composable("search"){
                SearchScreen(navController)
            }
            composable("profile"){
                ProfileScreen(navController, authViewModel, modifier)
            }
        }
    )
}