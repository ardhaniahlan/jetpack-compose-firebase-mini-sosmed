package org.apps.minisosmed

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import org.apps.minisosmed.navigation.MyBottomNavBar
import org.apps.minisosmed.navigation.MyNavigation
import org.apps.minisosmed.ui.theme.MiniSosmedTheme
import org.apps.minisosmed.viewmodel.AuthViewModel
import org.apps.minisosmed.viewmodel.ChatViewModel
import org.apps.minisosmed.viewmodel.CommentViewModel
import org.apps.minisosmed.viewmodel.PostViewModel
import org.apps.minisosmed.viewmodel.UserViewModel

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val snackbarHostState = remember { SnackbarHostState() }
            val navController = rememberNavController()

            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry?.destination?.route

            val bottomNavRoutes = listOf("home", "addpost", "search", "profile")

            MiniSosmedTheme {
                Scaffold(
                    bottomBar = {
                        if (bottomNavRoutes.any { currentDestination?.startsWith(it) == true }) {
                            MyBottomNavBar(navController)
                        }
                    },
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    MyNavigation(
                        modifier = Modifier.padding(innerPadding),
                        snackbarHostState = snackbarHostState,
                        navController = navController
                    )
                }
            }
        }
    }
}