package org.apps.minisosmed.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import org.apps.minisosmed.ui.theme.MiniSosmedTheme
import org.apps.minisosmed.viewmodel.AuthViewModel

@Composable
fun ProfileScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
    modifier: Modifier
){

    val uiState by authViewModel.uiState

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        ProfileScreenContent(
            onLogoutClick = {
                authViewModel.logout()
                navController.navigate("login") {
                    popUpTo("home") {
                        inclusive = true
                    }
                    launchSingleTop = true
                }
            }
        )

        if (uiState.isLoading){
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }
}

@Composable
fun ProfileScreenContent(
    onLogoutClick: () -> Unit
){


    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth()
                .padding( 20.dp),
            horizontalArrangement = Arrangement.End
        ) {
            Text(
                text = "Edit",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .clickable{  }
            )

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = "Logout",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Red,
                modifier = Modifier
                    .clickable{ onLogoutClick() }
            )
        }
    }
}

@Composable
@Preview(showBackground = true)
fun ProfileScreenContentPreview(){
    MiniSosmedTheme {
        ProfileScreenContent(onLogoutClick = {})
    }
}

