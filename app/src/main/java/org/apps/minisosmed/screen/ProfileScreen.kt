package org.apps.minisosmed.screen

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apps.minisosmed.R
import org.apps.minisosmed.entity.Post
import org.apps.minisosmed.entity.User
import org.apps.minisosmed.repository.ImageRepository
import org.apps.minisosmed.screen.component.PostItem
import org.apps.minisosmed.state.UiEvent
import org.apps.minisosmed.state.ViewState
import org.apps.minisosmed.ui.theme.MiniSosmedTheme
import org.apps.minisosmed.viewmodel.AuthViewModel
import org.apps.minisosmed.viewmodel.ChatViewModel
import org.apps.minisosmed.viewmodel.PostViewModel
import org.apps.minisosmed.viewmodel.UserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    modifier: Modifier,
    userId: String? = null,
    snackbarHostState: SnackbarHostState
){
    val userViewModel: UserViewModel = hiltViewModel()
    val chatViewModel: ChatViewModel = hiltViewModel()
    val authViewModel: AuthViewModel = hiltViewModel()
    val postViewModel: PostViewModel = hiltViewModel()

    val userState by userViewModel.uiState.collectAsState()
    val chatState by chatViewModel.uiState.collectAsState()

    var showCommentSheet by remember { mutableStateOf(false) }
    var selectedPostId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(userId) {
        if (userId != null) {
            userViewModel.getUserById(userId)
            userViewModel.getPostByUserId(userId)
        } else {
            userViewModel.loadCurrentUser()
            userViewModel.getCurrentUserPosts()
        }
    }

    LaunchedEffect(Unit) {
        authViewModel.eventFlow.collect { event ->
            when (event) {
                is UiEvent.Navigate -> {
                    navController.navigate("login") {
                        popUpTo(0)
                    }
                }

                else -> {}
            }
        }
    }

    LaunchedEffect(postViewModel.uiState) {
        postViewModel.eventFlow.collect { event ->
            when (event) {
                is UiEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(message = event.message)
                }
                else -> {}
            }
        }
    }

    LaunchedEffect(chatState.createChatState) {
        when (val state = chatState.createChatState) {
            is ViewState.Success -> {
                val chatId = state.data
                navController.navigate("chat/$chatId")
            }
            else -> {}
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            ProfileScreenTopBar(
                onEditClick = {
                    navController.navigate("editprofile")
                },
                onLogoutClick = {
                    authViewModel.logout()
                },
                isCurrentUser = (userId == null)
            )
        }
    ) { paddingValues ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = userState.userState) {
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
                        postsState = userState.postsState,
                        onMessageClick = {
                            if (userId != null) {
                                chatViewModel.openOrCreateChat(userId)
                            }
                        },
                        isCurrentUser = (userId == null),
                        onEditPost = { post ->
                            navController.navigate("editpost/${post.id}")
                        },
                        onDeleteClick = { postId ->
                            postViewModel.deletePost(postId)
                        },
                        onShowComments = { postId ->
                            selectedPostId = postId
                            showCommentSheet = true
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

        if (showCommentSheet && selectedPostId != null) {
            ModalBottomSheet(
                onDismissRequest = {
                    showCommentSheet = false
                    selectedPostId = null
                },
                modifier = Modifier.fillMaxHeight(),
                sheetState = rememberModalBottomSheetState(
                    skipPartiallyExpanded = true
                )
            ) {
                CommentBottomSheet(
                    postId = selectedPostId!!,
                    onDismiss = {
                        showCommentSheet = false
                        selectedPostId = null
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreenTopBar(
    onEditClick: () -> Unit,
    isCurrentUser: Boolean,
    onLogoutClick: () -> Unit,
) {
    TopAppBar(
        title = {
            Text(
                text = "Profil ${if (isCurrentUser) "Saya" else ""}",
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold,
            )
        },
        actions = {
            if (isCurrentUser) {
                TextButton(
                 onClick = onEditClick
                ) {
                    Text(
                        text = "Edit",
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                TextButton(
                    onClick = onLogoutClick,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = Color.Red
                    )
                ) {
                    Text(
                        text = "Logout",
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    )
}

@Composable
fun ProfileScreenContent(
    user: User,
    postsState: ViewState<List<Post>>,
    isCurrentUser: Boolean,
    onMessageClick: () -> Unit,
    onEditPost: (Post) -> Unit,
    onDeleteClick: (String) -> Unit,
    onShowComments: (String) -> Unit,
){

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, top = 16.dp, end = 16.dp),
            ) {
                Spacer(modifier = Modifier.width(20.dp))

                if (!user.photoUrl.isNullOrEmpty()) {
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

                Column {
                    Text(
                        text = user.displayName ?: "Guest",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                    )

                    Text(
                        text = user.email ?: "-",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                    )

                    if (!isCurrentUser) {
                        Button(
                            onClick = onMessageClick,
                            modifier = Modifier.padding(top = 16.dp)
                        ) {
                            Text("Message")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = user.bio ?: "-",
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                maxLines = 3
            )

            Text(
                text = "Posts",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 16.dp)
            )
        }

        when (postsState) {
            is ViewState.Loading -> {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }

            is ViewState.Success -> {
                val posts = postsState.data
                if (posts.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No posts yet",
                                color = Color.Gray,
                                fontSize = 16.sp
                            )
                        }
                    }
                } else {
                    items(posts) { post ->
                        PostItem(
                            post = post,
                            user = user,
                            isOwner = isCurrentUser && post.userId == user.id,
                            onEditPost = onEditPost,
                            onDeleteClick = onDeleteClick,
                            onShowComments = onShowComments,
                        )
                    }
                }
            }

            is ViewState.Error -> {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = postsState.message,
                            color = Color.Red,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            else -> Unit
        }
    }
}


