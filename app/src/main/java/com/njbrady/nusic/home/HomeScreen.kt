package com.njbrady.nusic.home


import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.njbrady.nusic.home.utils.rememberSwipeableCardState
import com.njbrady.nusic.home.utils.swipableCard
import com.njbrady.nusic.home.utils.Direction
import com.njbrady.nusic.home.responseObjects.SongObject
import com.njbrady.nusic.home.utils.SwipeableCardState
import com.njbrady.nusic.login.composables.CenteredProgressIndicator
import com.njbrady.nusic.login.composables.ErrorWithField
import com.njbrady.nusic.ui.theme.NusicTheme
import kotlinx.coroutines.launch


@Composable
fun HomeScreen(
    homeScreenViewModel: HomeScreenViewModel
) {
    HomeScreenContent(
        homeScreenViewModel = homeScreenViewModel,
    )
}

@Composable
private fun HomeScreenContent(
    homeScreenViewModel: HomeScreenViewModel,
) {
    val swipeableCardState = rememberSwipeableCardState()
    Scaffold(
        modifier = Modifier.windowInsetsPadding(
            WindowInsets.navigationBars.only(WindowInsetsSides.End)
        ), backgroundColor = MaterialTheme.colors.background
    ) { paddingValues ->
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues.calculateBottomPadding())
        ) {
            SongStack(
                homeScreenViewModel = homeScreenViewModel,
                paddingValues = paddingValues,
                modifier = Modifier.fillMaxSize(0.8f),
                swipeableCardState = swipeableCardState
            )
            FeedbackButtons(swipeableCardState = swipeableCardState,
                onLikeAction = { like -> homeScreenViewModel.likeTop(like) })
        }
    }
}

@Composable
private fun FeedbackButtons(
    modifier: Modifier = Modifier,
    swipeableCardState: SwipeableCardState,
    onLikeAction: (Boolean) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()

    Row(
        modifier = modifier
    ) {
        //dislike
        Button(modifier = Modifier.padding(horizontal = 8.dp),
            colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.error),
            onClick = {
                coroutineScope.launch {
                    swipeableCardState.finishSwipe(direction = Direction.Left)
                    swipeableCardState.resetInstant()
                    onLikeAction(false)
                }
            }) {
            Text(text = "Dislike")
        }
        Button(modifier = Modifier.padding(horizontal = 8.dp),
            colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.secondaryVariant),
            onClick = {
                coroutineScope.launch {
                    swipeableCardState.finishSwipe(direction = Direction.Right)
                    swipeableCardState.resetInstant()
                    onLikeAction(true)
                }
            }) {
            Text(text = "Like")
        }
    }
}


@Composable
private fun SongStack(
    homeScreenViewModel: HomeScreenViewModel,
    modifier: Modifier = Modifier,
    paddingValues: PaddingValues = PaddingValues(),
    swipeableCardState: SwipeableCardState
) {
    val upNow by homeScreenViewModel.upNow.collectAsState()
    val upNext by homeScreenViewModel.upNext.collectAsState()
    val upLast by homeScreenViewModel.upLast.collectAsState()
    val nonBlockingError by homeScreenViewModel.nonBlockingError.collectAsState()
    val loading by homeScreenViewModel.isLoading.collectAsState()
    val blockingError by homeScreenViewModel.blockingError.collectAsState()
    val blockingErrorToast by homeScreenViewModel.blockingErrorToast.collectAsState()


    Box(
        modifier = modifier.padding(paddingValues),
    ) {
        if (loading) {
            CenteredProgressIndicator(paddingValues = paddingValues)
        } else {
            ErrorScreen(
                modifier = Modifier.fillMaxSize(),
                onRetry = { homeScreenViewModel.retry() },
                blockingError = blockingError,
                nonBlockingError = nonBlockingError
            )
        }

        blockingErrorToast?.let {
            Toast.makeText(
                LocalContext.current,
                it,
                Toast.LENGTH_LONG
            ).show()
            homeScreenViewModel.resetToastErrors()
        }

        upLast?.let {
            SongCard(song = it, modifier = Modifier.fillMaxSize())
        }

        upNext?.let {
            SongCard(song = it, Modifier.fillMaxSize())
        }

        upNow?.let {
            SwipeableCard(onLikeAction = { songObject, liked ->
                homeScreenViewModel.likeSong(
                    songObject, liked
                )
            }, song = it, swipeableCardState = swipeableCardState)
        }
    }
}

@Composable
fun SwipeableCard(
    onLikeAction: (SongObject, Boolean) -> Unit,
    song: SongObject,
    swipeableCardState: SwipeableCardState
) {

    SongCard(
        song = song, modifier = Modifier
            .swipableCard(
                state = swipeableCardState,
                blockedDirections = listOf(Direction.Down),
                onSwiped = { dir ->
                    val liked = dir == Direction.Right
                    swipeableCardState.resetInstant()
                    onLikeAction(song, liked)
                },
            )
            .fillMaxSize()
    )
}

@Composable
private fun SongCard(
    song: SongObject,
    modifier: Modifier = Modifier,
) {

    Card(
        modifier = modifier.padding(horizontal = 8.dp, vertical = 8.dp),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, MaterialTheme.colors.onBackground),
        backgroundColor = MaterialTheme.colors.secondary
    ) {
        Row(modifier = Modifier, verticalAlignment = Alignment.Bottom) {
            Box(
                modifier = Modifier
                    .wrapContentHeight()
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(alpha = 0x10, red = 0x00, green = 0x00, blue = 0x00),
                                Color.Black
                            )
                        )
                    ),
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 32.dp)) {

                    song.name?.let {
                        Text(text = it, style = MaterialTheme.typography.h4, color = Color.White)
                    }
                    song.artist?.let {
                        Text(
                            text = "by $it",
                            style = MaterialTheme.typography.h5,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ErrorScreen(
    modifier: Modifier = Modifier,
    onRetry: () -> Unit,
    blockingError: String?,
    nonBlockingError: String?
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        blockingError?.let {
            ErrorWithField(
                message = it, modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )
        }
        nonBlockingError?.let {
            ErrorWithField(
                message = it, modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )
        }
        Button(modifier = Modifier, onClick = { onRetry() }) {
//            Text(text = "Retry")
            Icon(
                imageVector = Icons.Filled.Refresh,
                contentDescription = "Retry loading",
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    NusicTheme {
//        SongCard(song = SongObject("Name", "artist"), modifier = Modifier.fillMaxSize())
        Button(onClick = { /*TODO*/ }) {
            Icon(
                modifier = Modifier.fillMaxSize(),
                imageVector = Icons.Filled.Refresh,
                contentDescription = "Retry loading",
                tint = MaterialTheme.colors.error
            )
        }
    }
}



