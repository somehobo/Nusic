package com.njbrady.nusic.home


import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import com.njbrady.nusic.R
import com.njbrady.nusic.Screen
import com.njbrady.nusic.home.responseObjects.SongObject
import com.njbrady.nusic.home.utils.*
import com.njbrady.nusic.login.composables.CenteredProgressIndicator
import com.njbrady.nusic.login.composables.ErrorWithField
import com.njbrady.nusic.ui.theme.NusicTheme
import com.njbrady.nusic.home.utils.Direction
import kotlinx.coroutines.launch


@Composable
fun HomeScreen(
    homeScreenViewModel: HomeScreenViewModel, navController: NavController
) {
    navController.addOnDestinationChangedListener { _, destination, _ ->
        when (destination.route) {
            Screen.Home.route -> {
                homeScreenViewModel.resumeCurrentPreviousPlayState()
            }
            else -> {
                homeScreenViewModel.forcePauseCurrent()
            }
        }
    }
    HomeScreenContent(
        homeScreenViewModel = homeScreenViewModel,
    )
}

@Composable
private fun HomeScreenContent(
    homeScreenViewModel: HomeScreenViewModel,
) {
    val upNow by homeScreenViewModel.upNow.collectAsState()
    val upNext by homeScreenViewModel.upNext.collectAsState()
    val upLast by homeScreenViewModel.upLast.collectAsState()

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
                modifier = Modifier.fillMaxSize(0.9f),
                upNow = upNow,
                upNext = upNext,
                upLast = upLast
            )
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
        Button(modifier = Modifier.padding(horizontal = dimensionResource(id = R.dimen.NusicDimenX1)),
            colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.error),
            onClick = {
                coroutineScope.launch {
                    swipeableCardState.finishSwipe(direction = Direction.Left)
                    swipeableCardState.resetInstant()
                    onLikeAction(false)
                }
            }) {
            Text(text = stringResource(id = R.string.dislike_button_text))
        }
        Button(modifier = Modifier.padding(horizontal = dimensionResource(id = R.dimen.NusicDimenX1)),
            colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.secondaryVariant),
            onClick = {
                coroutineScope.launch {
                    swipeableCardState.finishSwipe(direction = Direction.Right)
                    swipeableCardState.resetInstant()
                    onLikeAction(true)
                }
            }) {
            Text(text = stringResource(id = R.string.like_button_text))
        }
    }
}


@Composable
private fun SongStack(
    homeScreenViewModel: HomeScreenViewModel,
    modifier: Modifier = Modifier,
    paddingValues: PaddingValues = PaddingValues(),
    upNow: SongCardState,
    upNext: SongCardState,
    upLast: SongCardState,
) {

    val upNowState by upNow.songCardStateState.collectAsState()
    val upNowError by upNow.errorMessage.collectAsState()
    val upNextState by upNext.songCardStateState.collectAsState()
    val upNextError by upNext.errorMessage.collectAsState()
    val upLastState by upLast.songCardStateState.collectAsState()
    val upLastError by upLast.errorMessage.collectAsState()
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
                LocalContext.current, it, Toast.LENGTH_LONG
            ).show()
            homeScreenViewModel.resetToastErrors()
        }



        SongCard(
            modifier = Modifier.fillMaxSize(),
            songCardStateState = upLastState,
            errorMessage = upLastError,
            songObject = upLast.songObject
        )


        SongCard(
            modifier = Modifier.fillMaxSize(),
            songCardStateState = upNextState,
            errorMessage = upNextError,
            songObject = upNext.songObject
        )


        SwipeableCardWrapper(modifier = Modifier.fillMaxSize(),
            songCardStateState = upNowState,
            errorMessage = upNowError,
            songObject = upNow.songObject,
            onRetry = { upNow.retry() },
            onCancel = { homeScreenViewModel.cancelTop() },
            onRestart = { upNow.restart() },
            onResume = { upNow.resume() },
            onPause = { upNow.pause() },
            onLiked = { like -> homeScreenViewModel.likeTop(like) })
    }
}


@Composable
private fun SwipeableCardWrapper(
    modifier: Modifier = Modifier,
    songCardStateState: SongCardStateStates,
    errorMessage: String,
    songObject: SongObject?,
    onRetry: () -> Unit,
    onRestart: () -> Unit,
    onResume: () -> Unit,
    onCancel: () -> Unit,
    onPause: () -> Unit,
    onLiked: (Boolean) -> Unit
) {
    val swipeableCardState = rememberSwipeableCardState()

    SongCard(
        modifier = modifier
            .swipableCard(
                state = swipeableCardState,
                blockedDirections = listOf(Direction.Down, Direction.Up),
                onSwiped = { dir ->
                    val liked = dir == Direction.Right
                    onLiked(liked)
                    swipeableCardState.resetInstant()
                },
            )
            .fillMaxSize()
            .clip(RoundedCornerShape(dimensionResource(id = R.dimen.NusicDimenX1)))
            .clickable {
                if (songCardStateState == SongCardStateStates.Playing) {
                    onPause()
                }
            },
        songCardStateState = songCardStateState,
        errorMessage = errorMessage,
        songObject = songObject,
        onRetry = onRetry,
        onRestart = onRestart,
        onResume = onResume,
        onCancel = onCancel,
    )
}


@Composable
fun SongCard(
    modifier: Modifier = Modifier,
    songCardStateState: SongCardStateStates,
    errorMessage: String,
    songObject: SongObject?,
    onRetry: () -> Unit = {},
    onRestart: () -> Unit = {},
    onResume: () -> Unit = {},
    onCancel: () -> Unit = {},
    cancelAvailable: Boolean = true
) {

    if (songCardStateState != SongCardStateStates.Empty) {
        Card(
            modifier = modifier,
            shape = RoundedCornerShape(dimensionResource(id = R.dimen.NusicDimenX1)),
            border = BorderStroke(
                dimensionResource(id = R.dimen.BorderStrokeSize), MaterialTheme.colors.onBackground
            ),
            backgroundColor = MaterialTheme.colors.secondary
        ) {
            Box(modifier = Modifier.fillMaxSize()) {

                when (songCardStateState) {
                    SongCardStateStates.Error -> SongCardErrorOverlay(modifier = Modifier
                        .fillMaxSize()
                        .zIndex(1f),
                        onRetry = { onRetry() },
                        onCancel = { onCancel() },
                        errorMessage = errorMessage,
                        cancelAvailable = cancelAvailable
                    )

                    SongCardStateStates.Completed -> SongCardCompletedOverlay(modifier = Modifier.zIndex(
                        1f
                    ), onReplay = { onRestart() })

                    SongCardStateStates.Paused -> SongCardPausedOverlay(modifier = Modifier.zIndex(
                        1f
                    ), onPlay = { onResume() }, onRestart = { onRestart() })

                    else -> {}
                }

                SongCardBottomContent(
                    modifier = Modifier
                        .wrapContentHeight()
                        .fillMaxWidth()
                        .zIndex(0f)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    colorResource(id = R.color.card_overlay), Color.Black
                                )
                            )

                        )
                        .align(Alignment.BottomCenter), songObject = songObject
                )
                if (songCardStateState == SongCardStateStates.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(dimensionResource(id = R.dimen.NusicDimenX7))
                    )
                }
            }
        }
    }
}

@Composable
private fun SongCardBottomContent(modifier: Modifier = Modifier, songObject: SongObject?) {
    Row(modifier = modifier, verticalAlignment = Alignment.Bottom) {
        Box(
            modifier = Modifier,
        ) {
            Column(
                modifier = Modifier.padding(
                    horizontal = dimensionResource(R.dimen.NusicDimenX2),
                    vertical = dimensionResource(R.dimen.NusicDimenX4)
                )
            ) {

                songObject?.name?.let {
                    Text(
                        text = it, style = MaterialTheme.typography.h4, color = Color.White
                    )
                }
                songObject?.artist?.let {
                    Text(
                        text = stringResource(id = R.string.author_creditor) + " $it",
                        style = MaterialTheme.typography.h5,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun SongCardErrorOverlay(
    modifier: Modifier = Modifier, onRetry: () -> Unit, onCancel: () -> Unit, errorMessage: String, cancelAvailable: Boolean
) {
    SongCardOverlay(modifier = modifier) {
        Column(
            modifier = Modifier
                .padding(
                    horizontal = dimensionResource(R.dimen.NusicDimenX2),
                    vertical = dimensionResource(R.dimen.NusicDimenX4)
                )
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ErrorWithField(
                modifier = Modifier.padding(vertical = dimensionResource(id = R.dimen.NusicDimenX1)),
                message = errorMessage,
                textColor = colorResource(id = R.color.white)
            )
            Row(horizontalArrangement = Arrangement.Center) {
                IconButton(onClick = { onRetry() }) {
                    Icon(
                        modifier = Modifier
                            .size(dimensionResource(id = R.dimen.NusicDimenX8))
                            .padding(
                                dimensionResource(id = R.dimen.NusicDimenX1)
                            ),
                        painter = painterResource(id = R.drawable.nusic_replay_icon),
                        contentDescription = "Play button",
                        tint = colorResource(
                            id = R.color.pause_play_color
                        )
                    )
                }
                if (cancelAvailable) {
                    IconButton(onClick = { onCancel() }) {
                        Icon(
                            modifier = Modifier
                                .size(dimensionResource(id = R.dimen.NusicDimenX8))
                                .padding(
                                    dimensionResource(id = R.dimen.NusicDimenX1)
                                ),
                            painter = painterResource(id = R.drawable.nusic_cancel_icon),
                            contentDescription = "Play button",
                            tint = colorResource(
                                id = R.color.pause_play_color
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SongCardPausedOverlay(
    modifier: Modifier = Modifier, onPlay: () -> Unit, onRestart: () -> Unit
) {
    SongCardOverlay(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .clickable { onPlay() },
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row {
                IconButton(onClick = { onPlay() }) {
                    Icon(
                        modifier = Modifier
                            .size(dimensionResource(id = R.dimen.NusicDimenX8))
                            .padding(
                                dimensionResource(id = R.dimen.NusicDimenX1)
                            ),
                        painter = painterResource(id = R.drawable.nusic_play_button),
                        contentDescription = "Play button",
                        tint = colorResource(
                            id = R.color.pause_play_color
                        )
                    )
                }

                IconButton(onClick = { onRestart() }) {
                    Icon(
                        modifier = Modifier
                            .size(dimensionResource(id = R.dimen.NusicDimenX8))
                            .padding(
                                dimensionResource(id = R.dimen.NusicDimenX1)
                            ),
                        painter = painterResource(id = R.drawable.nusic_replay_icon),
                        contentDescription = "Restart Button",
                        tint = colorResource(
                            id = R.color.pause_play_color
                        )
                    )
                }
            }
        }
    }
}


@Composable
private fun SongCardCompletedOverlay(
    modifier: Modifier = Modifier,
    onReplay: () -> Unit,
) {
    SongCardOverlay(modifier = modifier) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            IconButton(onClick = { onReplay() }) {
                Icon(
                    modifier = Modifier
                        .size(dimensionResource(id = R.dimen.NusicDimenX8))
                        .padding(
                            dimensionResource(id = R.dimen.NusicDimenX1)
                        ),
                    painter = painterResource(id = R.drawable.nusic_replay_icon),
                    contentDescription = "Restart Button",
                    tint = colorResource(
                        id = R.color.pause_play_color
                    )
                )
            }
        }
    }
}

@Composable
private fun SongCardOverlay(
    modifier: Modifier = Modifier, content: @Composable () -> Unit
) {
    Row(
        modifier = modifier, verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .wrapContentHeight()
                .fillMaxSize()
                .background(Color(R.color.card_overlay)),
        ) {
            content()
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
                message = it,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(dimensionResource(id = R.dimen.NusicDimenX2))
            )
        }
        nonBlockingError?.let {
            ErrorWithField(
                message = it,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(dimensionResource(id = R.dimen.NusicDimenX2))
            )
        }
        RetryButton {
            onRetry()
        }
    }
}

@Composable
fun RetryButton(modifier: Modifier = Modifier, callback: () -> Unit) {
    Button(modifier = modifier, onClick = { callback() }) {
        Icon(
            imageVector = Icons.Filled.Refresh,
            contentDescription = stringResource(id = R.string.refresh_button_content_description),
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    NusicTheme {
//        SongCardErrorOverlay(onRetry = {}, onCancel = {}, errorMessage = "HAI")
//        SongCardCompletedOverlay(onReplay = {})
//        SongCardPausedOverlay(onPlay = {}, onRestart = {})
    }
}



