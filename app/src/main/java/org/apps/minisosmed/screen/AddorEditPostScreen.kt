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
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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

    val isFormValid = remember(uiState.photoUrl, uiState.description) {
        val hasValidPhoto = uiState.photoUrl != null
        val hasValidDescription = uiState.description?.trim()?.isNotEmpty() == true

        hasValidPhoto || hasValidDescription
    }

    LaunchedEffect(Unit) {
        postViewModel.eventFlow.collect { event ->
            when (event) {
                is UiEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(message = event.message)
                    postViewModel.unblockUi()
                }
                UiEvent.Navigate -> {
                    navController.navigate("home") {
                        popUpTo("addpost") { inclusive = true }
                        launchSingleTop = true
                    }
                    postViewModel.unblockUi()
                }
            }
        }
    }

    LaunchedEffect(uiState.postOperation) {
        when (val state = uiState.postOperation) {
            is ViewState.Error -> {
                snackbarHostState.showSnackbar(message = state.message)
                postViewModel.resetPostState()
            }
            else -> {}
        }
    }

    val isEditMode = uiState.mode == PostMode.EDIT
    val isActionLoading = uiState.postOperation is ViewState.Loading || uiState.isUiBlocked

    val canSave = if (isEditMode) {
        uiState.description?.trim()?.isNotEmpty() == true && !isActionLoading
    } else {
        isFormValid && !isActionLoading
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            AddPostTopAppBar(
                isEditMode = isEditMode,
                onBackClick = {
                    if (!isActionLoading) navController.popBackStack()
                },
                onResetClick = {
                    if (!isActionLoading) postViewModel.clearForm()
                },
                onSaveClick = {
                    if (!isActionLoading) {
                        if (isEditMode) {
                            postViewModel.updatePost()
                        } else {
                            postViewModel.createPost(context)
                        }
                    }
                },
                enabled =
                    if (isEditMode) {
                    uiState.description?.trim()?.isNotEmpty() == true && !isActionLoading
                } else {
                    isFormValid && !isActionLoading
                },
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            AddPostScreenContent(
                uiState = uiState,
                onDescriptionChange = postViewModel::onDescriptionChange,
                onPickImageClick = {
                    if (!isActionLoading) imagePickerLauncher.launch("image/*")
                },
                isEditMode = isEditMode,
                enabled = !isActionLoading
            )

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
    onSaveClick: () -> Unit,
    enabled: Boolean = true
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
                IconButton(
                    onClick = onBackClick,
                    enabled = enabled
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            }
        },
        actions = {
            if (!isEditMode) {
                TextButton(
                    onClick = onResetClick,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = Color.Red
                    ),
                    enabled = enabled
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
                ),
                enabled = enabled
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
    isEditMode: Boolean,
    enabled: Boolean = true
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
                    if (!isEditMode) Modifier.clickable(enabled = enabled, onClick = onPickImageClick) else Modifier
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
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled

        )
    }
}