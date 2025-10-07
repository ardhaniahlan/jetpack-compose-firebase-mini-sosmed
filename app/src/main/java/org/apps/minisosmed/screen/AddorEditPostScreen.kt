package org.apps.minisosmed.screen

import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apps.minisosmed.entity.PostMode
import org.apps.minisosmed.repository.ImageRepository
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
            onSavePost = {
                if (uiState.mode == PostMode.EDIT) {
                    postViewModel.updatePost()
                } else {
                    postViewModel.createPost(context)
                }
                postViewModel.finishEditing()
            },
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

                postViewModel.clearMessage()
                postViewModel.finishEditing()

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
                postViewModel.clearMessage()
            }
        }
    }
}

@Composable
fun AddPostScreenContent(
    uiState: PostUiState,
    onDescriptionChange: (String) -> Unit,
    onPickImageClick: () -> Unit,
    onResetPost: () -> Unit,
    onSavePost: () -> Unit,
){
    val isEditMode = uiState.mode == PostMode.EDIT

    Row(
        modifier = Modifier.fillMaxWidth()
            .padding( 20.dp),
        horizontalArrangement = Arrangement.End
    ) {

        Text(
            text = if (isEditMode) "Edit Post" else "Add Post",
            fontSize = 25.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f)
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
            text = if (isEditMode) "Update" else "Post",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.clickable {
                if (isEditMode) onSavePost() else onSavePost()
            }
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
                .then(
                    if (!isEditMode) Modifier.clickable { onPickImageClick() }
                    else Modifier
                ),
            contentAlignment = Alignment.Center
        ) {
            when {
                isEditMode && uiState.photoUrl != null -> {
                    val bitmap by produceState<Bitmap?>(initialValue = null, key1 = uiState.photoUrl) {
                        value = withContext(Dispatchers.IO) {
                            ImageRepository().base64ToBitmap(
                                uiState.photoUrl.toString().substringAfter(",")
                            )
                        }
                    }

                    bitmap?.let {
                        Image(
                            bitmap = it.asImageBitmap(),
                            contentDescription = "Existing Post Image",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } ?: run {
                        Icon(
                            Icons.Default.Photo,
                            contentDescription = "Default Post",
                            modifier = Modifier.size(64.dp),
                            tint = Color.White
                        )
                    }
                }

                !isEditMode && uiState.photoUrl != null -> {
                    AsyncImage(
                        model = uiState.photoUrl,
                        contentDescription = "Picked Picture",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                else -> {
                    Icon(
                        Icons.Default.Photo,
                        contentDescription = "Default Post",
                        modifier = Modifier.size(64.dp),
                        tint = Color.White
                    )
                }
            }
        }

        if (isEditMode) {
            Text(
                text = "Foto tidak dapat diubah saat mode edit",
                color = Color.Gray,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
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