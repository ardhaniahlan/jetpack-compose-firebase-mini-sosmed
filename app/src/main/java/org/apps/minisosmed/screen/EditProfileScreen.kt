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
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
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
import androidx.navigation.NavController
import coil.compose.AsyncImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apps.minisosmed.entity.User
import org.apps.minisosmed.repository.ImageRepository
import org.apps.minisosmed.state.UpdateUserUiState
import org.apps.minisosmed.viewmodel.UserViewModel

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun EditProfileScreen(
    navController: NavController,
    userViewModel: UserViewModel,
    modifier: Modifier,
    snackbarHostState: SnackbarHostState
){

    val uiState by userViewModel.uiState
    val user by userViewModel.user.collectAsState()
    val context = LocalContext.current

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            uri?.let { userViewModel.onPhotoPicked(it) }
        }
    )

    Box(modifier = Modifier.fillMaxSize()) {
        EditProfileScreenContent(
            user = user,
            uiState = uiState,
            onDisplayNameChange = userViewModel::onDisplayNameChange,
            onBioChange = userViewModel::onBioChange,
            onPickImageClick = { imagePickerLauncher.launch("image/*") },
            onBackScreen = {
                navController.navigate("profile") {
                    popUpTo("editprofile") { inclusive = true }
                }
            },
            onSaveEdit = { userViewModel.updateProfile(context) }
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

                navController.navigate("profile") {
                    popUpTo("profile") { inclusive = true }
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

        userViewModel.refreshUser()
    }


}

@Composable
fun EditProfileScreenContent(
    user: User?,
    uiState: UpdateUserUiState,
    onBioChange: (String) -> Unit,
    onDisplayNameChange: (String) -> Unit,
    onPickImageClick: () -> Unit,
    onBackScreen: () -> Unit,
    onSaveEdit: () -> Unit,
){

    Row(
        modifier = Modifier.fillMaxWidth()
            .padding( 20.dp),
        horizontalArrangement = Arrangement.End
    ) {
        Icon(
            Icons.Default.ArrowBack,
            contentDescription = "Back",
            modifier = Modifier
                .size(30.dp)
                .clickable { onBackScreen() }
        )

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = "Edit Profil",
            fontSize = 25.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .weight(1f)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Icon(
            Icons.Default.Check,
            contentDescription = "Save",
            tint = if (uiState.isLoading) Color.Gray else MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .size(30.dp)
                .clickable(enabled = !uiState.isLoading) { onSaveEdit() }
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
                .size(120.dp)
                .clip(CircleShape)
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
                }
                user?.photoUrl != null -> {
                    val bitmap by produceState<Bitmap?>(initialValue = null, key1 = user.photoUrl) {
                        value = withContext(Dispatchers.IO) {
                            user.photoUrl.let { ImageRepository().base64ToBitmap(it) }
                        }
                    }

                    bitmap?.let {
                        Image(
                            bitmap = it.asImageBitmap(),
                            contentDescription = "Profile Picture",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
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
            modifier = Modifier.fillMaxWidth()
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
            maxLines = 3
        )
    }
}

//@Composable
//@Preview(showBackground = true)
//fun EditProfileScreenContentPreview() {
//    MiniSosmedTheme {
//        EditProfileScreenContent(
//            user = User(
//                id = "1",
//                displayName = "Ardhani Ahlan",
//                bio = "Orang Ganteng",
//                email = "ardhan@gmail.com",
//                photoUrl = "https://picsum.photos/200"
//            ),
//            uiState = UpdateUserUiState(
//                displayName = "Ardhani Ahlan",
//                bio = "Orang Ganteng",
//                photoUrl = null
//            ),
//            onDisplayNameChange = {},
//            onBioChange = {},
//            onPickImageClick = {},
//            onBackScreen = {},
//            onSaveEdit = {}
//        )
//    }
//}