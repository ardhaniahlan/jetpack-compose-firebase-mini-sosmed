package org.apps.minisosmed.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import org.apps.minisosmed.state.UiEvent
import org.apps.minisosmed.state.ViewState
import org.apps.minisosmed.ui.theme.poppinsFontFamily
import org.apps.minisosmed.viewmodel.AuthViewModel

@Composable
fun RegisterScreen(
    navController: NavController,
    modifier: Modifier,
    snackbarHostState: SnackbarHostState
) {
    val authViewModel: AuthViewModel = hiltViewModel()
    val uiState by authViewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        authViewModel.eventFlow.collect { event ->
            when (event) {
                is UiEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(message = event.message)
                    authViewModel.unblockUi()
                }
                UiEvent.Navigate -> {
                    navController.navigate("login") {
                        popUpTo("register") { inclusive = true }
                        launchSingleTop = true
                    }
                    authViewModel.unblockUi()
                }
            }
        }
    }

    val isActionLoading = uiState.authState is ViewState.Loading || uiState.isUiBlocked

    Box(
        modifier = modifier.fillMaxSize()
    ) {

        RegisterScreenContent(
            uiState = uiState,
            onDisplayNameChange = authViewModel::onDisplayNameChange,
            onEmailChange = authViewModel::onEmailChange,
            onPasswordChange = authViewModel::onPasswordChange,
            onConfirmPasswordChange = authViewModel::onConfirmPasswordChange,
            onVisibilityChange = authViewModel::togglePasswordVisibility,
            onCreateAccountClick = {
                if (!isActionLoading) authViewModel.register()
            },
            onLoginClick = {
                authViewModel.clearForm()
                navController.navigate("login") {
                    popUpTo("register") { inclusive = true }
                }
            },
            enabled = !isActionLoading
        )

        if (isActionLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
    }
}

@Composable
fun RegisterScreenContent(
    uiState: AuthUiState,
    onDisplayNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onVisibilityChange: () -> Unit,
    onCreateAccountClick: () -> Unit,
    onLoginClick: () -> Unit,
    enabled: Boolean = true
){
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(45.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Buat Akun",
                fontSize = 20.sp,
                fontFamily = poppinsFontFamily,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.End
            )

            Text(
                text = "Daftarkan akunmu pada Mini Sosmed",
                fontSize = 12.sp,
                fontFamily = poppinsFontFamily,
                fontWeight = FontWeight.Normal
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Display Name
            OutlinedTextField(
                value = uiState.displayName,
                onValueChange = onDisplayNameChange,
                label = { Text("Display Name") },
                isError = uiState.displayNameError != null,
                singleLine = true,
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Done
                ),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                enabled = enabled
            )
            uiState.displayNameError?.let {
                Text(it, color = Color.Red, fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

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
                enabled = enabled
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

                    IconButton(
                        onClick = onVisibilityChange,
                        enabled = enabled
                    ) {
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
                enabled = enabled
            )
            uiState.passwordError?.let {
                Text(it, color = Color.Red, fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Confirm Password
            OutlinedTextField(
                value = uiState.confirmPassword,
                onValueChange = onConfirmPasswordChange,
                label = { Text("Confirm Password") },
                placeholder = { Text("Confirm Password") },
                visualTransformation =
                    if (uiState.passwordVisible) VisualTransformation.None
                    else PasswordVisualTransformation(),
                trailingIcon = {
                    val image = if (uiState.passwordVisible) Icons.Default.Visibility
                    else Icons.Default.VisibilityOff

                    IconButton(
                        onClick = onVisibilityChange,
                        enabled = enabled
                    ) {
                        Icon(image, contentDescription = null)
                    }
                },
                isError = uiState.confirmPasswordError != null,
                singleLine = true,
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Done
                ),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                enabled = enabled
            )
            uiState.confirmPasswordError?.let {
                Text(it, color = Color.Red, fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = onCreateAccountClick,
                enabled = enabled
            ) { Text(text = "Registrasi") }

            Row {
                Text(
                    text = "Sudah punya akun?",
                    fontSize = 12.sp,
                    fontFamily = poppinsFontFamily,
                    fontWeight = FontWeight.Normal,
                )

                Spacer(modifier = Modifier.width(4.dp))

                Text(
                    text = "Login",
                    fontSize = 12.sp,
                    fontFamily = poppinsFontFamily,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.combinedClickable(
                        enabled = enabled,
                        onClick = onLoginClick
                    )
                )
            }
        }
    }
}




//@Preview(showBackground = true)
//@Composable
//fun RegisterScreenPreview() {
//    MiniSosmedTheme {
//        RegisterScreenContent(
//            uiState = AuthUiState(
//                displayName = "Ardhan",
//                email = "ardhan@gmail.com",
//                password = "password123",
//                confirmPassword = "password123",
//                passwordVisible = false,
//                displayNameError = null,
//                emailError = null,
//                passwordError = null,
//                confirmPasswordError = null
//            ),
//            onEmailChange = {},
//            onPasswordChange = {},
//            onCreateAccountClick = {},
//            onLoginClick = {},
//            onVisibilityChange = {},
//            onConfirmPasswordChange = {},
//            onDisplayNameChange = {}
//        )
//    }
//}