package org.apps.minisosmed.screen

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apps.minisosmed.entity.Post
import org.apps.minisosmed.entity.PostWithUser
import org.apps.minisosmed.entity.User
import org.apps.minisosmed.formatTimestamp
import org.apps.minisosmed.repository.ImageRepository
import org.apps.minisosmed.viewmodel.PostViewModel
import org.apps.minisosmed.viewmodel.UserViewModel

@Composable
fun HomeScreen(
    navController: NavController,
    postViewModel: PostViewModel,
) {
    val uiState by postViewModel.uiState

    LaunchedEffect(Unit) {
        postViewModel.loadPost()
    }

    HomeScreenContent(
        postsWithUser = uiState.postsWithUser,
        isLoading = uiState.isLoading,
        errorMessage = uiState.message,
    )
}


@Composable
fun HomeScreenContent(
    postsWithUser: List<PostWithUser>,
    isLoading: Boolean,
    errorMessage: String?,
) {

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth()
                .padding( 20.dp),
            horizontalArrangement = Arrangement.End
        ) {
            Text(
                text = "Mini Sosmed",
                fontSize = 25.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )
        }

        when {
            isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            errorMessage != null -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = "Error: $errorMessage", color = Color.Red)
                }
            }
            postsWithUser.isEmpty() -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Belum ada post")
                }
            }
            else -> {
                LazyColumn {
                    items(postsWithUser) { item ->
                        PostItem(
                            user = item.user,
                            post = item.post
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PostItem(
    user: User,
    post: Post
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            user.photoUrl?.let { base64 ->
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
                            .size(40.dp)
                            .clip(CircleShape)
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column {
                Text(
                    text = user.displayName ?: "Unknown",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = formatTimestamp(post.createdAt),
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Normal),
                )
            }

            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "Menu"
            )
        }

        post.photoUrl?.let { base64 ->
            val bitmap by produceState<Bitmap?>(initialValue = null, key1 = post.photoUrl) {
                value = withContext(Dispatchers.IO) {
                    post.photoUrl.let { ImageRepository().base64ToBitmap(it) }
                }
            }

            bitmap?.let {
                Image(
                    bitmap = it.asImageBitmap(),
                    contentDescription = "Post Image",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                )
            }
        }

        post.description?.takeIf { it.isNotBlank() }?.let {
            Text(
                text = it,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}



