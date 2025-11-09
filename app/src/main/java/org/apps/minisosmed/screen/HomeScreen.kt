package org.apps.minisosmed.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Message
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import org.apps.minisosmed.entity.relation.PostWithUser
import org.apps.minisosmed.screen.component.PostItem
import org.apps.minisosmed.state.UiEvent
import org.apps.minisosmed.state.ViewState
import org.apps.minisosmed.viewmodel.PostViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    modifier: Modifier,
    snackbarHostState: SnackbarHostState,
) {
    val postViewModel: PostViewModel = hiltViewModel()

    val uiState by postViewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        postViewModel.eventFlow.collect { event ->
            when (event) {
                is UiEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(message = event.message)
                }

                UiEvent.Navigate -> {
                    navController.popBackStack()
                }
            }
        }
    }

    LaunchedEffect(uiState.deletePostState) {
        when (val state = uiState.deletePostState) {
            is ViewState.Error -> {
                snackbarHostState.showSnackbar(message = state.message)
                postViewModel.resetPostState()
            }
            else -> {}
        }
    }

    var showCommentSheet by remember { mutableStateOf(false) }
    var selectedPostId by remember { mutableStateOf<String?>(null) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Mini Sosmed",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                actions = {
                    IconButton(onClick = { navController.navigate("chatList") }) {
                        Icon(
                            imageVector = Icons.Default.Message,
                            contentDescription = "Chat List"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->

        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            when (val userState = uiState.userState) {
                is ViewState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                is ViewState.Error -> {
                    Text(
                        text = userState.message,
                        color = Color.Red,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                is ViewState.Success -> {
                    when (val postState = uiState.postsState) {
                        is ViewState.Loading -> {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator()
                            }
                        }

                        is ViewState.Error -> {
                            Text(
                                text = postState.message,
                                color = Color.Red,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }

                        is ViewState.Success -> {
                            HomeScreenContent(
                                postsWithUser = postState.data,
                                currentUserId = userState.data.id,
                                navController = navController,
                                onDeletePost = { postId -> postViewModel.deletePost(postId) },
                                onShowComments = { postId ->
                                    selectedPostId = postId
                                    showCommentSheet = true
                                }
                            )
                        }

                        else -> Unit
                    }
                }

                else -> Unit
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
}

@Composable
fun HomeScreenContent(
    postsWithUser: List<PostWithUser>,
    currentUserId: String?,
    navController: NavController,
    onDeletePost: (String) -> Unit,
    onShowComments: (String) -> Unit
) {

    if (postsWithUser.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Belum ada post")
        }
    } else {
        LazyColumn {
            items(postsWithUser) { item ->
                PostItem(
                    user = item.user,
                    post = item.post,
                    isOwner = item.post.userId == currentUserId,
                    onEditPost = { post ->
                        navController.navigate("editpost/${post.id}")
                    },
                    onDeleteClick = onDeletePost,
                    onShowComments = onShowComments
                )
            }
        }
    }
}





