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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apps.minisosmed.entity.PostMode
import org.apps.minisosmed.repository.ImageRepository
import org.apps.minisosmed.state.PostUiState
import org.apps.minisosmed.state.UiEvent
import org.apps.minisosmed.state.ViewState
import org.apps.minisosmed.viewmodel.PostViewModel

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AddPostScreen(
    navController: NavController,
    modifier: Modifier,
    snackbarHostState: SnackbarHostState,
    postViewModel: PostViewModel
){
    val uiState by postViewModel.uiState.collectAsState()
    val context = LocalContext.current

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            uri?.let { postViewModel.onPhotoPicked(it) }
        }
    )

    LaunchedEffect(Unit) {
        postViewModel.eventFlow.collect { event ->
            when (event) {
                is UiEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(message = event.message)
                }
                UiEvent.Navigate -> {
                    navController.navigate("home") {
                        popUpTo("addpost") { inclusive = true }
                        launchSingleTop = true
                    }
                }
            }
        }
    }

    LaunchedEffect(uiState.createPostState) {
        when (val state = uiState.createPostState) {
            is ViewState.Error -> {
                snackbarHostState.showSnackbar(message = state.message)
                postViewModel.resetPostState()
            }
            else -> {}
        }
    }

    LaunchedEffect(uiState.updatePostState) {
        when (val state = uiState.updatePostState) {
            is ViewState.Error -> {
                snackbarHostState.showSnackbar(message = state.message)
                postViewModel.resetPostState()
            }
            else -> {}
        }
    }

    val isEditMode = uiState.mode == PostMode.EDIT

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            AddPostTopAppBar(
                isEditMode = isEditMode,
                onBackClick = {
                    navController.popBackStack()
                },
                onResetClick = { postViewModel.clearForm() },
                onSaveClick = {
                    if (isEditMode) {
                        postViewModel.updatePost()
                    } else {
                        postViewModel.createPost(context)
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(modifier = modifier.fillMaxSize().padding(paddingValues)) {
            AddPostScreenContent(
                uiState = uiState,
                onDescriptionChange = postViewModel::onDescriptionChange,
                onPickImageClick = { imagePickerLauncher.launch("image/*") },
                isEditMode = isEditMode,
            )

            val isActionLoading = uiState.createPostState is ViewState.Loading ||
                    uiState.updatePostState is ViewState.Loading

            if (isActionLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPostTopAppBar(
    isEditMode: Boolean,
    onBackClick: () -> Unit,
    onResetClick: () -> Unit,
    onSaveClick: () -> Unit
) {
    TopAppBar(
        title = {
            Text(
                text = if (isEditMode) "Edit Post" else "Add Post",
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold
            )
        },
        navigationIcon = {
            if (isEditMode) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    modifier = Modifier
                        .size(30.dp)
                        .clickable { onBackClick() }
                )
            }
        },
        actions = {
            if (!isEditMode) {
                TextButton(
                    onClick = onResetClick,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = Color.Red
                    )
                ) {
                    Text(
                        text = "Reset",
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))
            }

            Spacer(modifier = Modifier.width(8.dp))

            TextButton(
                onClick = onSaveClick,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = if (isEditMode) "Update" else "Post",
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    )
}

@Composable
fun AddPostScreenContent(
    uiState: PostUiState,
    onDescriptionChange: (String) -> Unit,
    onPickImageClick: () -> Unit,
    isEditMode: Boolean
){
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .padding(30.dp)
            .verticalScroll(scrollState)
            .fillMaxWidth(),
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