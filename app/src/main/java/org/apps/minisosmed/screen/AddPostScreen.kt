package org.apps.minisosmed.screen

import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import org.apps.minisosmed.state.PostUiState
import org.apps.minisosmed.ui.theme.MiniSosmedTheme
import org.apps.minisosmed.viewmodel.PostViewModel

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AddPostScreen(
    navController: NavController,
    postViewModel: PostViewModel,
    modifier: Modifier,
    snackbarHostState: SnackbarHostState
){
    val uiState by postViewModel.uiState
    val context = LocalContext.current

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            uri?.let { postViewModel.onPhotoPicked(it) }
        }
    )

    Box(modifier = Modifier.fillMaxSize()) {
        AddPostScreenContent(
            uiState = uiState,
            onDescriptionChange = postViewModel::onDescriptionChange,
            onPickImageClick = { imagePickerLauncher.launch("image/*") },
            onSavePost = { postViewModel.createPost(context) },
            onResetPost = { postViewModel.clearForm() }
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

    LaunchedEffect(uiState.success, uiState.message) {
        when {
            uiState.success != null -> {
                snackbarHostState.showSnackbar(
                    message = uiState.success!!,
                    duration = SnackbarDuration.Short,
                    actionLabel = "OK"
                )

                navController.navigate("home") {
                    popUpTo("addpost") { inclusive = true }
                    launchSingleTop = true
                }
            }

            uiState.message != null -> {
                snackbarHostState.showSnackbar(
                    message = uiState.message!!,
                    duration = SnackbarDuration.Short
                )
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            postViewModel.resetUiState()
        }
    }
}

@Composable
fun AddPostScreenContent(
    uiState: PostUiState,
    onDescriptionChange: (String) -> Unit,
    onPickImageClick: () -> Unit,
    onResetPost: () -> Unit,
    onSavePost: () -> Unit
){
    Row(
        modifier = Modifier.fillMaxWidth()
            .padding( 20.dp),
        horizontalArrangement = Arrangement.End
    ) {

        Text(
            text = "Edit Profil",
            fontSize = 25.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .weight(1f)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = "Reset",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.Red,
            modifier = Modifier
                .clickable { onResetPost() }
        )

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = "Post",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.clickable { onSavePost() }
        )
    }

    Column(
        modifier = Modifier
            .padding(45.dp)
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Box(
            modifier = Modifier
                .padding(top=50.dp)
                .size(200.dp)
                .background(Color.Gray)
                .clickable { onPickImageClick() },
            contentAlignment = Alignment.Center
        ) {
            when {
                uiState.photoUrl != null -> {
                    AsyncImage(
                        model = uiState.photoUrl,
                        contentDescription = "Picked Picture",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else -> {
                    Icon(
                        Icons.Default.Photo,
                        contentDescription = "Default Post",
                        modifier = Modifier.size(64.dp),
                        tint = Color.White
                    )
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        OutlinedTextField(
            value = uiState.description!!,
            onValueChange = onDescriptionChange,
            label = { Text("Deskripsi") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
@Preview(showBackground = true)
fun AddPostScreenContentPreview() {
    MiniSosmedTheme {
        AddPostScreenContent(
            uiState = PostUiState(
                description = "Ardhani Ahlan",
                photoUrl = null
            ),
            onDescriptionChange = {},
            onPickImageClick = {},
            onSavePost = {},
            onResetPost = {},
        )
    }
}