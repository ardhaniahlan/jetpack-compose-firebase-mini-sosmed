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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import org.apps.minisosmed.state.AuthUiState
import org.apps.minisosmed.state.ViewState
import org.apps.minisosmed.ui.theme.poppinsFontFamily
import org.apps.minisosmed.viewmodel.AuthViewModel

@Composable
fun LoginScreen(
    navController: NavController,
    modifier: Modifier,
    snackbarHostState: SnackbarHostState
){
    val authViewModel: AuthViewModel = hiltViewModel()
    val uiState by authViewModel.uiState
    val authState by authViewModel.authState.collectAsState()

    Box(
        modifier = modifier.fillMaxSize()
    ){
        LoginScreenContent(
            uiState = uiState,
            onEmailChange = authViewModel::onEmailChange,
            onPasswordChange = authViewModel::onPasswordChange,
            onVisibilityChange = authViewModel::togglePasswordVisibility,
            onLoginAccountClick = { authViewModel.login() },
            onRegisterClick = {
                authViewModel.clearForm()
                navController.navigate("register") {
                    popUpTo("login") { inclusive = true }
                }
            }
        )

        when (val state = authState) {
            is ViewState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is ViewState.Success -> {
                LaunchedEffect(Unit) {
                    snackbarHostState.showSnackbar(
                        message = "Login Berhasil",
                    )

                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            }

            is ViewState.Error -> {
                LaunchedEffect(state.message) {
                    snackbarHostState.showSnackbar(
                        message = state.message,
                    )
                    authViewModel.resetAuthState()
                }
            }

            else -> Unit
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            authViewModel.clearForm()
            authViewModel.resetAuthState()
        }
    }

}

@Composable
fun LoginScreenContent(
    uiState: AuthUiState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onVisibilityChange : () -> Unit,
    onLoginAccountClick: () -> Unit,
    onRegisterClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = "Mini Sosmed",
            fontSize = 32.sp,
            fontFamily = poppinsFontFamily,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 60.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 45.dp, top = 80.dp, end = 45.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Login Akun",
                fontSize = 20.sp,
                fontFamily = poppinsFontFamily,
                fontWeight = FontWeight.SemiBold,
            )

            Text(
                text = "Masuk untuk menemukan inspirasi dan bagikan momen terbaikmu",
                fontSize = 12.sp,
                fontFamily = poppinsFontFamily,
                fontWeight = FontWeight.Normal,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Email
            OutlinedTextField(
                value = uiState.email,
                onValueChange = onEmailChange,
                label = { Text("Email") },
                isError = uiState.emailError != null,
                singleLine = true,
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Done
                ),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
            )
            uiState.emailError?.let {
                Text(it, color = Color.Red, fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = uiState.password,
                onValueChange = onPasswordChange,
                label = { Text("Password") },
                visualTransformation =
                    if (uiState.passwordVisible) VisualTransformation.None
                    else PasswordVisualTransformation(),
                trailingIcon = {
                    val image = if (uiState.passwordVisible) Icons.Default.Visibility
                    else Icons.Default.VisibilityOff

                    IconButton(onClick = onVisibilityChange) {
                        Icon(image, contentDescription = null)
                    }
                },
                isError = uiState.passwordError != null,
                singleLine = true,
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Done
                ),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
            )
            uiState.passwordError?.let {
                Text(it, color = Color.Red, fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = onLoginAccountClick
            ) { Text(text = "Login") }

            Row {
                Text(
                    text = "Belum punya akun?",
                    fontSize = 12.sp,
                    fontFamily = poppinsFontFamily,
                    fontWeight = FontWeight.Normal,
                )

                Spacer(modifier = Modifier.width(4.dp))

                Text(
                    text = "Register",
                    fontSize = 12.sp,
                    fontFamily = poppinsFontFamily,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.clickable{
                        onRegisterClick()
                    }
                )
            }
        }
    }
}

//@Preview(showBackground = true)
//@Composable
//fun LoginScreenPreview() {
//    MiniSosmedTheme {
//        LoginScreenContent(
//            uiState = AuthUiState(
//                email = "ardhan@gmail.com",
//                password = "password123",
//                passwordVisible = false,
//                emailError = null,
//                passwordError = null,
//            ),
//            onEmailChange = {},
//            onPasswordChange = {},
//            onLoginAccountClick = {},
//            onRegisterClick = {},
//            onVisibilityChange = {},
//        )
//    }
//}