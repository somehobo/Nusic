package com.njbrady.nusic.upload

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.snapping.SnapLayoutInfoProvider
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.njbrady.nusic.LocalNavController
import com.njbrady.nusic.R
import com.njbrady.nusic.login.composables.CenteredProgressIndicator
import com.njbrady.nusic.login.composables.ErrorWithField
import com.njbrady.nusic.ui.theme.NusicBlue
import com.njbrady.nusic.ui.theme.NusicSeeThroughBlack
import com.njbrady.nusic.ui.theme.NusicTheme
import com.njbrady.nusic.utils.composables.EditableSongCard
import com.njbrady.nusic.utils.composables.NavigationTopAppBar

@Composable
fun UploadScreen(
    uploadScreenViewModel: UploadScreenViewModel, navController: NavController
) {
    val localNavController = LocalNavController.current
    val generalLoading by uploadScreenViewModel.generalLoading.collectAsState()
    Scaffold(topBar = {
        val localContext = LocalContext.current
        NavigationTopAppBar(navController = navController, title = "Upload", actions = {
            if (generalLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.padding(
                        end = dimensionResource(id = R.dimen.NusicDimenX1)
                    )
                )
            } else {
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
                    onClick = { uploadScreenViewModel.attemptUpload(localContext) },
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
            }
        })
    }) { paddingValues ->
        UploadScreenContent(
            uploadScreenViewModel = uploadScreenViewModel, onUpload = {
                navController.navigateUp()
                uploadScreenViewModel.clearState()
            }, paddingValues = paddingValues, generalLoading = generalLoading
        )
    }
}

@Composable
private fun UploadScreenContent(
    uploadScreenViewModel: UploadScreenViewModel,
    generalLoading: Boolean,
    onUpload: () -> Unit,
    paddingValues: PaddingValues
) {
    val songErrors by uploadScreenViewModel.songErrors.collectAsState()
    val photoErrors by uploadScreenViewModel.photoErrors.collectAsState()
    val generalErrors by uploadScreenViewModel.generalErrors.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
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

            photoErrors?.forEach {
                ErrorWithField(
                    message = it,
                    modifier = Modifier.padding(dimensionResource(id = R.dimen.NusicDimenX1))
                )
            }

            UploadSong(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(dimensionResource(id = R.dimen.NusicDimenX15)),
                uploadScreenViewModel = uploadScreenViewModel,
                onUpload = onUpload
            )

            songErrors?.forEach {
                ErrorWithField(
                    message = it,
                    modifier = Modifier.padding(dimensionResource(id = R.dimen.NusicDimenX1))
                )
            }
        }
        if (generalLoading) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color(R.color.card_overlay))
            )
            {
                Surface(modifier = Modifier.fillMaxSize(), color = Color.Transparent){}
            }
        }
    }



    generalErrors?.forEach {
        Toast.makeText(
            LocalContext.current, it, Toast.LENGTH_LONG
        ).show()
    }
    uploadScreenViewModel.clearToastErrors()
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
    val uploadSuccess by uploadScreenViewModel.successfulUpload.collectAsState()
    if (uploadSuccess) {
        Toast.makeText(
            LocalContext.current, "Upload Complete", Toast.LENGTH_LONG
        ).show()
        onUpload()
    }
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
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
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
                            onChangeSong = {
                                selectSongLauncher.launch(
                                    "audio/*"
                                )
                            },
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
                    else -> {}
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
        Column(modifier = Modifier.fillMaxSize()) {
            if (scrollEnabled) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(dimensionResource(id = R.dimen.NusicDimenX4)),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    currentLoc?.let {
                        Text(
                            text = calculateMinuteFormat(it),
                        )
                    }
                    Text(
                        modifier = Modifier.padding(horizontal = dimensionResource(id = R.dimen.NusicDimenX2)),
                        text = calculateMinuteFormat(start) + " - " + calculateMinuteFormat(
                            end
                        )
                    )


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
                    val boxColor =if (!scrollEnabled) barColor else if (index == currentLoc) currentLocColor
                    else if (index in start..end) barColor
                    else colorResource(id = R.color.nusic_card_grey)
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
}

fun calculateMinuteFormat(totalSeconds: Int): String {
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    val secondsRep = if (seconds < 10) "0$seconds" else "$seconds"
    return "$minutes:$secondsRep"
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
