package com.njbrady.nusic.upload

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.snapping.SnapLayoutInfoProvider
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.gestures.stopScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Done
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.njbrady.nusic.R
import com.njbrady.nusic.login.composables.CenteredProgressIndicator
import com.njbrady.nusic.ui.theme.NusicBlue
import com.njbrady.nusic.ui.theme.NusicSeeThroughBlack
import com.njbrady.nusic.ui.theme.NusicTheme
import com.njbrady.nusic.utils.composables.EditableSongCard
import com.njbrady.nusic.utils.composables.NavigationTopAppBar
import kotlinx.coroutines.flow.MutableStateFlow

@Composable
fun UploadScreen(
    uploadScreenViewModel: UploadScreenViewModel, navController: NavController
) {
    Scaffold(topBar = {
        NavigationTopAppBar(navController = navController, title = "Upload", actions = {
            IconButton(
                onClick = { uploadScreenViewModel.clearState() },
                modifier = Modifier
                    .size(dimensionResource(id = R.dimen.NusicDimenX5))
                    .padding(
                        end = dimensionResource(id = R.dimen.NusicDimenX1)
                    )
            ) {
                Icon(
                    modifier = Modifier.size(dimensionResource(id = R.dimen.NusicDimenX5)),
                    imageVector = Icons.Outlined.Close,
                    contentDescription = "Cancel Button"
                )

            }
            IconButton(
                onClick = { /* on upload here */ },
                modifier = Modifier
                    .size(dimensionResource(id = R.dimen.NusicDimenX5))
                    .padding(
                        end = dimensionResource(id = R.dimen.NusicDimenX1)
                    )
            ) {
                Icon(
                    modifier = Modifier.size(dimensionResource(id = R.dimen.NusicDimenX5)),
                    imageVector = Icons.Outlined.Done,
                    tint = NusicBlue,
                    contentDescription = "Upload Button"
                )
            }
        })
    }) { paddingValues ->
        UploadScreenContent(
            uploadScreenViewModel = uploadScreenViewModel,
            onUpload = { navController.navigateUp() },
            paddingValues = paddingValues
        )
    }
}

@Composable
private fun UploadScreenContent(
    uploadScreenViewModel: UploadScreenViewModel, onUpload: () -> Unit, paddingValues: PaddingValues
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues = paddingValues)
    ) {
        EditableSongCard(
            viewModel = uploadScreenViewModel,
            modifier = Modifier
                .fillMaxSize()
                .padding(dimensionResource(id = R.dimen.NusicDimenX1))
                .weight(1f)
        )
        //FFT Upload thing
        UploadSong(
            modifier = Modifier
                .fillMaxWidth()
                .height(dimensionResource(id = R.dimen.NusicDimenX15)),
            uploadScreenViewModel = uploadScreenViewModel,
            onUpload = onUpload
        )
    }
}

@Composable
private fun UploadSong(
    modifier: Modifier = Modifier,
    uploadScreenViewModel: UploadScreenViewModel,
    onUpload: () -> Unit
) {
    val playerState by uploadScreenViewModel.uploadSongPlayerState.collectAsState()
    val songAmplitude by uploadScreenViewModel.songAmplitude.collectAsState()
    val uploadSongLoading by uploadScreenViewModel.uploadSongLoading.collectAsState()
    val songUrl by uploadScreenViewModel.songUrl.collectAsState()
    val localContext = LocalContext.current
    val songStart by uploadScreenViewModel.uploadSongStartTime.collectAsState()
    val songEnd by uploadScreenViewModel.uploadSongEndTime.collectAsState()
    val curPos by uploadScreenViewModel.uploadSongCurPos.collectAsState()
    val selectSongLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { uri: Uri? ->
        if (uri != null) {
            uploadScreenViewModel.setSongUrl(
                uri = uri, context = localContext
            )
        }
    }

    Column(modifier = modifier) {
        if (uploadSongLoading) {
            CenteredProgressIndicator()
        } else {
            if (songUrl == null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(
                            start = dimensionResource(
                                id = R.dimen.NusicDimenX1
                            ), end = dimensionResource(
                                id = R.dimen.NusicDimenX1
                            ), bottom = dimensionResource(
                                id = R.dimen.NusicDimenX1
                            )
                        )
                        .clip(RoundedCornerShape(dimensionResource(id = R.dimen.NusicDimenX1)))
                        .background(MaterialTheme.colors.primary)
                        .clickable {
                            selectSongLauncher.launch(
                                "audio/*"
                            )
                        }, contentAlignment = Alignment.Center
                ) {
                    Amplitude(
                        modifier = Modifier.padding(vertical = dimensionResource(id = R.dimen.NusicDimenX1)),
                        barColor = MaterialTheme.colors.primaryVariant
                    )
                    Icon(
                        modifier = Modifier.size(dimensionResource(id = R.dimen.NusicDimenX7)),
                        imageVector = Icons.Filled.AddCircle,
                        contentDescription = "Add Image"
                    )
                }
            } else {
                Row(Modifier.fillMaxSize()) {
                    UploadSongOptions(
                        modifier = Modifier
                            .fillMaxHeight()
                            .padding(dimensionResource(id = R.dimen.NusicDimenX1))
                            .width(dimensionResource(id = R.dimen.NusicDimenX5)),
                        onTogglePlay = { uploadScreenViewModel.togglePlayState() },
                        onChangeSong = { },
                        playState = playerState
                    )
                    Amplitude(modifier = Modifier.padding(vertical = dimensionResource(id = R.dimen.NusicDimenX1)),
                        data = songAmplitude,
                        barColor = MaterialTheme.colors.primary,
                        scrollEnabled = true,
                        start = songStart,
                        end = songEnd,
                        currentLoc = curPos,
                        setStart = { start -> uploadScreenViewModel.setStartTime(start) })
                }
            }
        }
    }
}

@Composable
private fun UploadSongOptions(
    modifier: Modifier = Modifier,
    onTogglePlay: () -> Unit,
    onChangeSong: () -> Unit,
    playState: PlayerState
) {
    Column(modifier = modifier) {
        Column(
            modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(1f),
                contentPadding = PaddingValues(0.dp),
                onClick = { onTogglePlay() },
                shape = CircleShape
            ) {
                when (playState) {
                    PlayerState.Paused -> {
                        Icon(
                            modifier = Modifier.fillMaxSize(0.75f),
                            imageVector = Icons.Filled.PlayArrow,
                            contentDescription = "Play uploaded song"
                        )
                    }
                    PlayerState.Playing -> {
                        Icon(
                            modifier = Modifier.fillMaxSize(0.75f),
                            imageVector = Icons.Filled.Pause,
                            contentDescription = "Pause uploaded song"
                        )
                    }
                    PlayerState.Loading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.fillMaxSize(0.75f), color = Color.Gray
                        )
                    }
                }
            }
            Box(
                modifier = Modifier.height(dimensionResource(id = R.dimen.NusicDimenX1))
            )
            Button(
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(1f),
                contentPadding = PaddingValues(0.dp),
                onClick = { onChangeSong() },
                shape = CircleShape
            ) {
                Icon(
                    modifier = Modifier.fillMaxSize(0.75f),
                    imageVector = Icons.Filled.SwapHoriz,
                    contentDescription = "Change songs"
                )
            }
        }
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun Amplitude(
    modifier: Modifier,
    data: List<Float> = UploadScreenViewModel.DEFAULT_SONG,
    barColor: Color,
    currentLocColor: Color = MaterialTheme.colors.secondary,
    scrollEnabled: Boolean = false,
    start: Int = 0,
    end: Int = 30,
    currentLoc: Int? = null,
    setStart: (Int) -> Unit = {}
) {
    BoxWithConstraints(modifier = modifier) {
        val remainingFill =
            maxWidth - (30 * dimensionResource(id = R.dimen.NusicDimenXHalf).value).dp
        val lazyListState = rememberLazyListState(initialFirstVisibleItemIndex = start)
        if (scrollEnabled) {
            LaunchedEffect(Unit) {
                snapshotFlow { lazyListState.firstVisibleItemIndex }.collect { firstItemIndex ->
                    setStart(firstItemIndex)
                }
            }
        }
        LazyRow(
            modifier = Modifier.fillMaxSize(),
            userScrollEnabled = scrollEnabled,
            verticalAlignment = Alignment.CenterVertically,
            state = lazyListState,
            flingBehavior = rememberSnapFlingBehavior(
                snapLayoutInfoProvider = SnapLayoutInfoProvider(
                    lazyListState = lazyListState,
                    positionInLayout = { _, _ -> 0f })
            )
        ) {
            itemsIndexed(items = data) { index, item ->
                val boxColor =
                    if (index == currentLoc)
                        currentLocColor
                    else if (index in start..end)
                        barColor
                    else
                        colorResource(id = R.color.nusic_card_grey)
                Box(
                    modifier = Modifier
                        .width(dimensionResource(id = R.dimen.NusicDimenXHalf))
                        .fillMaxHeight(item)
                        .background(boxColor)
                        .border(
                            border = BorderStroke(
                                width = dimensionResource(id = R.dimen.BorderStrokeSizeXHalf),
                                color = NusicSeeThroughBlack
                            )
                        )
                )
            }
            item {
                Box(
                    modifier = modifier
                        .fillParentMaxHeight()
                        .width(remainingFill)
                )
            }
        }
    }
}


@Composable
@Preview(showBackground = true)
private fun defaultPreview() {
    NusicTheme {
        UploadSongOptions(
            modifier = Modifier.size(120.dp),
            onChangeSong = {},
            onTogglePlay = {},
            playState = PlayerState.Playing
        )
    }
}
