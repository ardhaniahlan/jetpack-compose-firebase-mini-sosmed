package org.apps.minisosmed.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
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
import org.apps.minisosmed.viewmodel.AuthViewModel
import org.apps.minisosmed.viewmodel.ChatViewModel
import org.apps.minisosmed.viewmodel.CommentViewModel
import org.apps.minisosmed.viewmodel.PostViewModel
import org.apps.minisosmed.viewmodel.UserViewModel

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MyNavigation(
    authViewModel: AuthViewModel,
    userViewModel: UserViewModel,
    postViewModel: PostViewModel,
    chatViewModel: ChatViewModel,
    commentViewModel: CommentViewModel,
    modifier: Modifier,
    snackbarHostState: SnackbarHostState,
    navController: NavHostController,
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
                HomeScreen(navController, postViewModel, commentViewModel,modifier, snackbarHostState )
            }
            composable("addpost"){
                AddPostScreen(navController, postViewModel, modifier, snackbarHostState)
            }
            composable("search"){
                SearchScreen(navController, userViewModel)
            }
            composable("profile"){
                ProfileScreen(navController, authViewModel, userViewModel,chatViewModel, modifier)
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
                    authViewModel = authViewModel,
                    userViewModel = userViewModel,
                    chatViewModel = chatViewModel,
                    modifier = modifier,
                    userId = userId
                )
            }

            composable(
                route = "addpost?postId={postId}",
                arguments = listOf(
                    navArgument("postId") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    }
                )
            ) { backStackEntry ->
                val postId = backStackEntry.arguments?.getString("postId")

                LaunchedEffect(postId) {
                    if (postId != null) {
                        val post = postViewModel.uiState.value.postsWithUser.find { it.post.id == postId }?.post
                        post?.let { postViewModel.startEditPost(it) }
                    } else {
                        postViewModel.resetPostState()
                    }
                }

                AddPostScreen(
                    navController = navController,
                    postViewModel = postViewModel,
                    modifier = modifier,
                    snackbarHostState = snackbarHostState
                )
            }

            composable("chatList") {
                ChatListScreen(
                    navController = navController,
                    chatViewModel = chatViewModel,
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
                    chatViewModel = chatViewModel
                )
            }
        }
    )
}