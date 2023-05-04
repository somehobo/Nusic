package com.njbrady.nusic.home


import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.njbrady.nusic.LocalNavController
import com.njbrady.nusic.R
import com.njbrady.nusic.Screen
import com.njbrady.nusic.login.composables.CenteredProgressIndicator
import com.njbrady.nusic.login.composables.ErrorWithField
import com.njbrady.nusic.ui.theme.NusicTheme
import com.njbrady.nusic.upload.PlayerState
import com.njbrady.nusic.utils.SongPlayerWrapper
import com.njbrady.nusic.utils.composables.SongCard
import com.njbrady.nusic.utils.composables.SwipeableCardWrapper


@Composable
fun HomeScreen(
    homeScreenViewModel: HomeScreenViewModel, navController: NavController
) {
    navController.addOnDestinationChangedListener { _, destination, _ ->
        when (destination.route) {
            Screen.Home.route -> {
                homeScreenViewModel.ifTempPauseThenResume()
            }
            else -> {
                homeScreenViewModel.tempPauseCurrent()
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
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues.calculateBottomPadding())
        ) {
            SongStack(
                homeScreenViewModel = homeScreenViewModel,
                paddingValues = paddingValues,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(dimensionResource(id = R.dimen.NusicDimenX1)),
                songQueue = songQueue
            )
        }
    }
}


@Composable
private fun SongStack(
    homeScreenViewModel: HomeScreenViewModel,
    modifier: Modifier = Modifier,
    paddingValues: PaddingValues = PaddingValues(),
    songQueue: List<SongPlayerWrapper>
) {
    val localNavController = LocalNavController.current
    val topCardState by homeScreenViewModel.topSongState.collectAsState()
    val topCardErrorMessage by homeScreenViewModel.topSongErrorMessage.collectAsState()
    val nonBlockingError by homeScreenViewModel.nonBlockingError.collectAsState()
    val loading by homeScreenViewModel.isLoading.collectAsState()
    val blockingError by homeScreenViewModel.blockingError.collectAsState()
    val blockingErrorToast by homeScreenViewModel.blockingErrorToast.collectAsState()
    val psd by homeScreenViewModel.psd.collectAsState()


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

        songQueue.asReversed().forEachIndexed { index, songPlayerWrapper ->
            key(songPlayerWrapper.uuid) {
                if (index == songQueue.lastIndex) {
                    SwipeableCardWrapper(modifier = Modifier.fillMaxSize(),
                        playerState = topCardState,
                        errorMessage = topCardErrorMessage,
                        songModel = songPlayerWrapper.songModel,
                        psd = psd,
                        onRetry = { songPlayerWrapper.reset() },
                        onCancel = { homeScreenViewModel.cancelTop() },
                        onRestart = { songPlayerWrapper.restart() },
                        onResume = { songPlayerWrapper.play() },
                        onPause = { homeScreenViewModel.pauseCurrent() },
                        onLiked = { like -> homeScreenViewModel.likeTop(like) },
                        onUserNameTap = { userModel ->
                            if (userModel.id != homeScreenViewModel.userModel.id)
                                localNavController.navigate(
                                    Screen.OtherProfile.createRoute(
                                        userName = userModel.userName, userId = userModel.id
                                    )
                                ) {
                                    restoreState = true
                                } else {
                                localNavController.navigate(Screen.Profile.route) {
                                    popUpTo(localNavController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = false
                                }
                            }

                        })
                } else {
                    SongCard(
                        modifier = Modifier.fillMaxSize(),
                        playerState = PlayerState.Playing,
                        errorMessage = "",
                        songModel = songPlayerWrapper.songModel
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



