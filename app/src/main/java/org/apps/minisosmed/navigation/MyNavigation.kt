package org.apps.minisosmed.navigation

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.google.firebase.auth.FirebaseAuth
import org.apps.minisosmed.screen.AddPostScreen
import org.apps.minisosmed.screen.ChatListScreen
import org.apps.minisosmed.screen.ChatScreen
import org.apps.minisosmed.screen.EditProfileScreen
import org.apps.minisosmed.screen.HomeScreen
import org.apps.minisosmed.screen.LoginScreen
import org.apps.minisosmed.screen.ProfileScreen
import org.apps.minisosmed.screen.RegisterScreen
import org.apps.minisosmed.screen.SearchScreen
import org.apps.minisosmed.screen.SplashScreen
import org.apps.minisosmed.viewmodel.PostViewModel

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MyNavigation(
    modifier: Modifier,
    snackbarHostState: SnackbarHostState,
    navController: NavHostController,
) {
    NavHost(
        navController = navController,
        startDestination = "splash",
        builder = {
            composable("login"){
                LoginScreen(navController, modifier, snackbarHostState)
            }
            composable("register"){
                RegisterScreen(navController, modifier, snackbarHostState)
            }
            composable("splash"){
                SplashScreen(navController)
            }
            composable("editprofile"){
                EditProfileScreen(navController, modifier, snackbarHostState)
            }

            // Bottom Bar
            composable("home"){
                HomeScreen(navController, modifier, snackbarHostState )
            }
            composable("addpost") {
                val postViewModel: PostViewModel = hiltViewModel()
                postViewModel.resetPostState()
                AddPostScreen(navController, modifier, snackbarHostState, postViewModel)
            }
            composable("search"){
                SearchScreen(navController)
            }
            composable("profile"){
                ProfileScreen(navController, modifier)
            }

            composable(
                route = "editpost/{postId}",
                arguments = listOf(navArgument("postId") { type = NavType.StringType })
            ) { backStackEntry ->
                val postId = backStackEntry.arguments?.getString("postId")!!
                val postViewModel: PostViewModel = hiltViewModel()

                LaunchedEffect(postId) {
                    postViewModel.loadPostById(postId)
                }

                AddPostScreen(navController, modifier, snackbarHostState, postViewModel)
            }

            composable(
                route = "profile/{userId}",
                arguments = listOf(
                    navArgument("userId") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    }
                )
            ) { backStackEntry ->
                val userId = backStackEntry.arguments?.getString("userId")

                ProfileScreen(
                    navController = navController,
                    modifier = modifier,
                    userId = userId
                )
            }

            composable("chatList") {
                ChatListScreen(
                    navController = navController,
                    currentUserId = FirebaseAuth.getInstance().currentUser?.uid!!
                )
            }

            composable(
                route = "chat/{chatId}",
                arguments = listOf(navArgument("chatId") { type = NavType.StringType })
            ) { backStackEntry ->
                val chatId = backStackEntry.arguments?.getString("chatId")!!
                ChatScreen(
                    chatId = chatId,
                    currentUserId = FirebaseAuth.getInstance().currentUser?.uid!!,
                )
            }
        }
    )
}