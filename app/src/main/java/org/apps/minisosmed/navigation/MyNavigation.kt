package org.apps.minisosmed.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import org.apps.minisosmed.screen.AddPostScreen
import org.apps.minisosmed.screen.EditProfileScreen
import org.apps.minisosmed.screen.HomeScreen
import org.apps.minisosmed.screen.LoginScreen
import org.apps.minisosmed.screen.ProfileScreen
import org.apps.minisosmed.screen.RegisterScreen
import org.apps.minisosmed.screen.SearchScreen
import org.apps.minisosmed.screen.SplashScreen
import org.apps.minisosmed.viewmodel.AuthViewModel
import org.apps.minisosmed.viewmodel.PostViewModel
import org.apps.minisosmed.viewmodel.UserViewModel

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MyNavigation(
    authViewModel: AuthViewModel,
    userViewModel: UserViewModel,
    postViewModel: PostViewModel,
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
            composable("editprofile"){
                EditProfileScreen(navController, userViewModel, modifier, snackbarHostState)
            }

            // Bottom Bar
            composable("home"){
                HomeScreen(navController, postViewModel, modifier, snackbarHostState)
            }
            composable("addpost"){
                AddPostScreen(navController, postViewModel, modifier, snackbarHostState)
            }
            composable("search"){
                SearchScreen(navController)
            }
            composable("profile"){
                ProfileScreen(navController, authViewModel, userViewModel, modifier)
            }
        }
    )
}