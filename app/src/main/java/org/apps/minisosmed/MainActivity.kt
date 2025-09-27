package org.apps.minisosmed

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import org.apps.minisosmed.di.Injection
import org.apps.minisosmed.navigation.MyNavigation
import org.apps.minisosmed.ui.theme.MiniSosmedTheme
import org.apps.minisosmed.viewmodel.AuthViewModel
import org.apps.minisosmed.viewmodel.ViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val authViewModel: AuthViewModel by viewModels {
            ViewModelFactory(Injection.provideAuthRepository())
        }

        setContent {
            val snackbarHostState = remember { SnackbarHostState() }
            MiniSosmedTheme {
                Scaffold(
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    MyNavigation(
                        modifier = Modifier.padding(innerPadding),
                        authViewModel = authViewModel,
                        snackbarHostState = snackbarHostState
                    )
                }
            }
        }
    }
}