package com.njbrady.nusic.home


import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
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
import com.njbrady.nusic.home.utils.SwipeableCardState
import com.njbrady.nusic.home.utils.Direction
import kotlinx.coroutines.launch


@Composable
fun HomeScreen(
    homeScreenViewModel: HomeScreenViewModel, navController: NavController
) {
    navController.addOnDestinationChangedListener { _, destination, _ ->
        when (destination.route) {
            Screen.Home.route -> {
                homeScreenViewModel.resumeCurrent()
            }
            else -> {
                homeScreenViewModel.pauseCurrent()
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
                LocalContext.current, it, Toast.LENGTH_LONG
            ).show()
            homeScreenViewModel.resetToastErrors()
        }

        SongCard(songCardState = upLast, modifier = Modifier.fillMaxSize())

        SongCard(songCardState = upNext, modifier = Modifier.fillMaxSize())

        SwipeableCard(onLikeAction = { songObject, liked ->
            homeScreenViewModel.likeSong(
                songObject, liked
            )
        },
            songCardState = upNow,
            swipeableCardState = swipeableCardState,
            onCancel = { homeScreenViewModel.cancelTop() })
    }
}

@Composable
private fun SwipeableCard(
    onLikeAction: (SongObject?, Boolean) -> Unit,
    onCancel: () -> Unit,
    songCardState: SongCardState,
    swipeableCardState: SwipeableCardState
) {

    SongCard(
        songCardState = songCardState, onCancel = onCancel, modifier = Modifier
            .swipableCard(
                state = swipeableCardState,
                blockedDirections = listOf(Direction.Down, Direction.Up),
                onSwiped = { dir ->
                    val liked = dir == Direction.Right
                    swipeableCardState.resetInstant()
                    onLikeAction(songCardState.songObject, liked)
                },
            )
            .fillMaxSize()
    )
}

@Composable
private fun SongCard(
    modifier: Modifier = Modifier,
    songCardState: SongCardState,
    onCancel: () -> Unit = {},
) {
    val currentState by songCardState.songCardStateState.collectAsState()
    val errorMessage by songCardState.errorMessage.collectAsState()
    if (currentState != SongCardStateStates.Empty) {
        Card(
            modifier = modifier.padding(
                horizontal = dimensionResource(id = R.dimen.NusicDimenX1),
                vertical = dimensionResource(
                    id = R.dimen.NusicDimenX1
                )
            ),
            shape = RoundedCornerShape(dimensionResource(id = R.dimen.NusicDimenX1)),
            border = BorderStroke(
                dimensionResource(id = R.dimen.BorderStrokeSize), MaterialTheme.colors.onBackground
            ),
            backgroundColor = MaterialTheme.colors.secondary
        ) {
            if (currentState == SongCardStateStates.Error) {
                SongCardErrorOverlay(
                    modifier = Modifier
                        .fillMaxSize()
                        .zIndex(1f),
                    onRetry = { songCardState.retry() },
                    onCancel = { onCancel() },
                    errorMessage = errorMessage
                )
            }
            SongCardBottomContent(
                modifier = Modifier
                    .wrapContentHeight()
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(R.color.card_overlay), Color.Black
                            )
                        )
                    ), songCardState = songCardState
            )
        }
        if (currentState == SongCardStateStates.Loading) {
            CircularProgressIndicator(modifier = Modifier.size(dimensionResource(id = R.dimen.NusicDimenX7)))
        }
    }
}

@Composable
private fun SongCardBottomContent(modifier: Modifier = Modifier, songCardState: SongCardState) {
    Row(modifier = Modifier, verticalAlignment = Alignment.Bottom) {
        Box(
            modifier = modifier,
        ) {
            Column(
                modifier = Modifier.padding(
                    horizontal = dimensionResource(R.dimen.NusicDimenX2),
                    vertical = dimensionResource(R.dimen.NusicDimenX4)
                )
            ) {

                songCardState.songObject?.name?.let {
                    Text(
                        text = it, style = MaterialTheme.typography.h4, color = Color.White
                    )
                }
                songCardState.songObject?.artist?.let {
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
    modifier: Modifier = Modifier, onRetry: () -> Unit, onCancel: () -> Unit, errorMessage: String
) {
    Row(
        modifier = modifier, verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .wrapContentHeight()
                .fillMaxWidth()
                .background(Color(R.color.card_overlay)),
        ) {
            Column(
                modifier = Modifier.padding(
                    horizontal = dimensionResource(R.dimen.NusicDimenX2),
                    vertical = dimensionResource(R.dimen.NusicDimenX4)
                )
            ) {
                ErrorWithField(message = errorMessage)
                Row(horizontalArrangement = Arrangement.Center) {
                    RetryButton {
                        onRetry()
                    }
                    Button(onClick = { onCancel() }) {
                        Icon(
                            imageVector = Icons.Filled.Cancel,
                            contentDescription = stringResource(id = R.string.cancel_button_content_description),
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
        SongCard(songCardState = SongCardState(), onCancel = { /*TODO*/ })
    }
}



