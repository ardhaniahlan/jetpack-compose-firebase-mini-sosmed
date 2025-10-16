package org.apps.minisosmed.screen

import android.graphics.Bitmap
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apps.minisosmed.R
import org.apps.minisosmed.entity.User
import org.apps.minisosmed.repository.ImageRepository
import org.apps.minisosmed.state.ViewState
import org.apps.minisosmed.ui.theme.MiniSosmedTheme
import org.apps.minisosmed.viewmodel.AuthViewModel
import org.apps.minisosmed.viewmodel.UserViewModel

@Composable
fun ProfileScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
    userViewModel: UserViewModel,
    modifier: Modifier
){
    val userState by userViewModel.user.collectAsState()

    LaunchedEffect(Unit) {
        userViewModel.refreshUser()
    }

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        when (val state = userState) {
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
                val user = state.data
                ProfileScreenContent(
                    user = user,
                    onLogoutClick = {
                        authViewModel.logout()
                        navController.navigate("login") {
                            popUpTo("home") { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    onEditClick = {
                        navController.navigate("editprofile")
                    }
                )
            }

            is ViewState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = state.message,
                        color = Color.Red,
                        fontSize = 14.sp
                    )
                }
            }

            else -> Unit
        }
    }
}

@Composable
fun ProfileScreenContent(
    user: User?,
    onLogoutClick: () -> Unit,
    onEditClick: () -> Unit
){

    Column(
        modifier = Modifier.fillMaxSize(),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth()
                .padding( 20.dp),
            horizontalArrangement = Arrangement.End
        ) {
            Text(
                text = "Profil Saya",
                fontSize = 25.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )

            Text(
                text = "Edit",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .clickable{ onEditClick() }
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

        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 16.dp, top = 16.dp, end = 16.dp),
        ) {
            Spacer(modifier = Modifier.width(20.dp))

            if (!user?.photoUrl.isNullOrEmpty()) {
                val bitmap by produceState<Bitmap?>(initialValue = null, key1 = user.photoUrl) {
                    value = withContext(Dispatchers.IO) {
                        user.photoUrl.let { ImageRepository().base64ToBitmap(it) }
                    }
                }


                bitmap?.let {
                    Image(
                        bitmap = it.asImageBitmap(),
                        contentDescription = "Profile Picture",
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(Color.Gray),
                        contentScale = ContentScale.Crop
                    )
                }
            } else {
                Image(
                    painter = painterResource(id = R.drawable.ic_launcher_background),
                    contentDescription = "Profile Picture",
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(Color.Gray),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column{
                Text(
                    text = user?.displayName ?: "Guest",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                )

                Text(
                    text = user?.email ?: "-",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = user?.bio ?: "-",
            fontSize = 16.sp,
            fontWeight = FontWeight.Normal,
            modifier = Modifier.fillMaxWidth().padding( 20.dp),
            maxLines = 3
        )
    }
}

@Composable
@Preview(showBackground = true)
fun ProfileScreenContentPreview() {
    MiniSosmedTheme {
        ProfileScreenContent(
            user = User(
                id = "1",
                displayName = "Ardhani Ahlan",
                email = "ardhan@gmail.com",
                bio = "Saya ganteng",
                photoUrl = "https://picsum.photos/200"
            ),
            onLogoutClick = {},
            onEditClick = {}
        )
    }
}


