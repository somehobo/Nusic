package com.njbrady.nusic.home


import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import com.njbrady.nusic.R
import com.njbrady.nusic.Screen
import com.njbrady.nusic.home.utils.Direction
import com.njbrady.nusic.home.utils.SongCardState
import com.njbrady.nusic.home.utils.SwipeableCardState
import com.njbrady.nusic.login.composables.CenteredProgressIndicator
import com.njbrady.nusic.login.composables.ErrorWithField
import com.njbrady.nusic.ui.theme.NusicTheme
import com.njbrady.nusic.utils.composables.SongCard
import com.njbrady.nusic.utils.composables.SwipeableCardWrapper
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
    val songQueue by homeScreenViewModel.realSongQueue.collectAsState()

    Scaffold(
        modifier = Modifier.windowInsetsPadding(
            WindowInsets.navigationBars.only(WindowInsetsSides.End)
        ), backgroundColor = MaterialTheme.colors.background
    ) { paddingValues ->
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize().padding(paddingValues.calculateBottomPadding())
        ) {
            SongStack(
                homeScreenViewModel = homeScreenViewModel,
                paddingValues = paddingValues,
                modifier = Modifier.fillMaxSize()
                    .padding(dimensionResource(id = R.dimen.NusicDimenX1)),
                songQueue = songQueue
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
    songQueue: List<SongCardState>
) {

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

        songQueue.asReversed().forEachIndexed { index, songCardState ->
            val songCardStateState by songCardState.songCardStateState.collectAsState()
            val songCardStateError by songCardState.errorMessage.collectAsState()
            key(songCardState.songObject?.songId) {
                if (index == 0) {
                    SwipeableCardWrapper(modifier = Modifier.fillMaxSize(),
                        songCardStateState = songCardStateState,
                        errorMessage = songCardStateError,
                        songObject = songCardState.songObject,
                        onRetry = { songCardState.retry() },
                        onCancel = { homeScreenViewModel.cancelTop() },
                        onRestart = { songCardState.restart() },
                        onResume = { songCardState.resume() },
                        onPause = { songCardState.pause() },
                        onLiked = { like -> homeScreenViewModel.likeTop(like) })
                } else {
                    SongCard(
                        modifier = Modifier.fillMaxSize(),
                        songCardStateState = songCardStateState,
                        errorMessage = songCardStateError,
                        songObject = songCardState.songObject
                    )
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
                modifier = Modifier.fillMaxWidth()
                    .padding(dimensionResource(id = R.dimen.NusicDimenX2))
            )
        }
        nonBlockingError?.let {
            ErrorWithField(
                message = it,
                modifier = Modifier.fillMaxWidth()
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



