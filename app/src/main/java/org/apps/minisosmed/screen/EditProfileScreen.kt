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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apps.minisosmed.entity.User
import org.apps.minisosmed.repository.ImageRepository
import org.apps.minisosmed.state.UiEvent
import org.apps.minisosmed.state.UpdateUserUiState
import org.apps.minisosmed.state.ViewState
import org.apps.minisosmed.viewmodel.UserViewModel

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun EditProfileScreen(
    navController: NavController,
    modifier: Modifier,
    snackbarHostState: SnackbarHostState
){
    val parentEntry = remember(navController.currentBackStackEntry) {
        navController.getBackStackEntry("profile")
    }
    val userViewModel: UserViewModel = hiltViewModel(parentEntry)
    val uiState by userViewModel.uiState.collectAsState()
    val context = LocalContext.current

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            uri?.let { userViewModel.onPhotoPicked(it) }
        }
    )

    LaunchedEffect(Unit) {
        userViewModel.eventFlow.collect { event ->
            when (event) {
                is UiEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(message = event.message)
                    userViewModel.unblockUi()
                }
                UiEvent.Navigate -> {
                    navController.navigate("profile") {
                        popUpTo("editprofile") { inclusive = true }
                        launchSingleTop = true
                    }
                    userViewModel.unblockUi()
                }
            }
        }
    }

    LaunchedEffect(uiState.userState) {
        when (val state = uiState.userState) {
            is ViewState.Success -> {
                val user = state.data
                userViewModel.preFillForm(user)
            }
            else -> {}
        }
    }

    val isActionLoading = uiState.updateState is ViewState.Loading || uiState.isUiBlocked

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            EditProfileTopBar(
                onBackClick = {
                    if (!isActionLoading) navController.popBackStack()
                },
                onSaveEdit = {
                    if (!isActionLoading) userViewModel.updateProfile(context)
                },
                enabled = !isActionLoading
            )
        }
    ) { paddingValues ->

        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            EditProfileScreenContent(
                uiState = uiState,
                onDisplayNameChange = userViewModel::onDisplayNameChange,
                onBioChange = userViewModel::onBioChange,
                onPickImageClick = {
                    if (!isActionLoading) imagePickerLauncher.launch("image/*")
                },
                enabled = !isActionLoading
            )

            if (isActionLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileTopBar(
    onBackClick: () -> Unit,
    enabled: Boolean = true,
    onSaveEdit: () -> Unit,
) {
    TopAppBar(
        title = {
            Text(
                text = "Edit Profile",
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold
            )
        },
        navigationIcon = {
            IconButton(
                onClick = onBackClick,
                enabled = enabled
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back"
                )
            }
        },
        actions = {
            val isLoading = false
            Icon(
                Icons.Default.Check,
                contentDescription = "Save",
                tint = if (isLoading) Color.Gray else MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(30.dp)
                    .clickable(enabled = !isLoading) { onSaveEdit() }
            )
        }
    )
}

@Composable
fun EditProfileScreenContent(
    uiState: UpdateUserUiState,
    onBioChange: (String) -> Unit,
    onDisplayNameChange: (String) -> Unit,
    onPickImageClick: () -> Unit,
    enabled: Boolean = true
){
    Column(
        modifier = Modifier
            .padding(45.dp)
            .fillMaxSize()
            .clickable(
                enabled = enabled,
                onClick = onPickImageClick
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Box(
            modifier = Modifier
                .padding(top=50.dp)
                .size(120.dp)
                .clip(CircleShape)
                .background(Color.Gray),
            contentAlignment = Alignment.Center
        ) {
            when {
                uiState.selectedImageUri != null -> {
                    AsyncImage(
                        model = uiState.selectedImageUri,
                        contentDescription = "Picked Picture",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                }

                uiState.photoUrl != null -> {
                    val bitmap by produceState<Bitmap?>(initialValue = null, key1 = uiState.photoUrl) {
                        value = withContext(Dispatchers.IO) {
                            uiState.photoUrl.let { ImageRepository().base64ToBitmap(it.toString()) }
                        }
                    }

                    bitmap?.let {
                        Image(
                            bitmap = it.asImageBitmap(),
                            contentDescription = "Profile Picture",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                } else -> {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = "Default Avatar",
                        modifier = Modifier.size(64.dp),
                        tint = Color.White
                    )
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        OutlinedTextField(
            value = uiState.displayName!!,
            onValueChange = onDisplayNameChange,
            label = { Text("Display Name") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled
        )
        uiState.displayNameError?.let {
            Text(it, color = Color.Red, fontSize = 12.sp)
        }

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = uiState.bio!!,
            onValueChange = onBioChange,
            label = { Text("Bio") },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 3,
            enabled = enabled
        )
    }
}