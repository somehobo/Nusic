package com.njbrady.nusic.utils.composables

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.core.view.WindowCompat
import coil.compose.SubcomposeAsyncImage
import com.njbrady.nusic.R
import com.njbrady.nusic.home.model.SongModel
import com.njbrady.nusic.home.utils.Direction
import com.njbrady.nusic.home.utils.SongCardStateStates
import com.njbrady.nusic.home.utils.rememberSwipeableCardState
import com.njbrady.nusic.home.utils.swipeableCard
import com.njbrady.nusic.login.composables.ErrorWithField
import com.njbrady.nusic.upload.UploadScreenViewModel
import com.njbrady.nusic.utils.shimmerBackground

@Composable
fun SwipeableCardWrapper(
    modifier: Modifier = Modifier,
    songCardStateState: SongCardStateStates,
    errorMessage: String,
    songObject: SongModel?,
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
            .swipeableCard(
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
private fun BaseSongCard(modifier: Modifier, content: @Composable () -> Unit) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(dimensionResource(id = R.dimen.NusicDimenX1)),
        border = BorderStroke(
            dimensionResource(id = R.dimen.BorderStrokeSize),
            colorResource(id = R.color.nusic_card_grey)
        ),
        backgroundColor = colorResource(id = R.color.nusic_card_grey)
    ) {
        content()
    }
}

@Composable
private fun EditableImageBackground(
    modifier: Modifier = Modifier, viewModel: UploadScreenViewModel
) {
    val localFocusManager = LocalFocusManager.current
    val photoUrl by viewModel.songPhotoUrl.collectAsState()
    val selectImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.setPhotoUrl(uri = uri)
        }
    }

    Box(modifier = modifier.clickable {
        selectImageLauncher.launch(
            "image/*"
        )
    }) {
        if (photoUrl != null) {
            SubcomposeAsyncImage(
                modifier = Modifier.fillMaxSize(),
                model = photoUrl,
                loading = {
                    Box(
                        modifier = Modifier
                            .background(colorResource(id = R.color.card_overlay))
                            .fillMaxSize()
                            .shimmerBackground(),
                    )
                },
                contentScale = ContentScale.Crop,
                contentDescription = stringResource(R.string.current_songs_image)
            )
        }
    }
}

@Composable
fun EditableSongCard(
    viewModel: UploadScreenViewModel, modifier: Modifier
) {
    val localFocusManager = LocalFocusManager.current
    val keyBoardPresent by keyboardAsState()
    BaseSongCard(modifier = modifier) {
        EditableImageBackground(
            modifier = Modifier.fillMaxSize(), viewModel = viewModel
        )
        if (keyBoardPresent == Keyboard.Opened) {
            Box(modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures(onTap = {
                        localFocusManager.clearFocus()
                    })
                })
        }

        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            EditableSongCardBottomContent(
                modifier = Modifier
                    .wrapContentHeight()
                    .fillMaxWidth()
                    .zIndex(0f)
                    .align(Alignment.BottomCenter), viewModel = viewModel
            )
        }
    }
}


@Composable
fun SongCard(
    modifier: Modifier = Modifier,
    songCardStateState: SongCardStateStates,
    errorMessage: String,
    songObject: SongModel?,
    onRetry: () -> Unit = {},
    onRestart: () -> Unit = {},
    onResume: () -> Unit = {},
    onCancel: () -> Unit = {},
    cancelAvailable: Boolean = true
) {

    if (songCardStateState != SongCardStateStates.Empty) {
        BaseSongCard(modifier = modifier) {
            songObject?.imageUrl?.let {
                SubcomposeAsyncImage(
                    modifier = Modifier.fillMaxSize(),
                    model = it,
                    loading = {
                        Box(
                            modifier = Modifier
                                .background(colorResource(id = R.color.card_overlay))
                                .fillMaxSize()
                                .shimmerBackground(),
                        )
                    },
                    contentScale = ContentScale.Crop,
                    contentDescription = stringResource(R.string.current_songs_image)
                )
            }

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
private fun BaseSongCardBottomContent(modifier: Modifier, content: @Composable () -> Unit) {

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
                content()
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun EditableSongCardBottomContent(
    modifier: Modifier = Modifier, viewModel: UploadScreenViewModel
) {
    val localFocusManager = LocalFocusManager.current
    val songTitle by viewModel.songTitle.collectAsState()
    val adaptiveTextName = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        letterSpacing = 0.25.sp,
        background = MaterialTheme.colors.onBackground,
        color = MaterialTheme.colors.background
    )

    val adaptiveTextNamePlaceHolder = MaterialTheme.typography.subtitle1.copy(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        letterSpacing = 0.25.sp
    )

    val adaptiveTextCreator = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        letterSpacing = 0.25.sp,
        background = MaterialTheme.colors.onBackground,
        color = MaterialTheme.colors.background
    )

    BaseSongCardBottomContent(modifier = modifier) {

        TextField(
            modifier = Modifier.padding(bottom = dimensionResource(id = R.dimen.NusicDimenX1)),
            value = songTitle,
            onValueChange = { newTitle -> viewModel.setSongTitle(newTitle) },
            textStyle = adaptiveTextName,
            placeholder = { Text(text = "Song Title", style = adaptiveTextNamePlaceHolder) },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { localFocusManager.clearFocus() }),
        )

        viewModel.username?.let {
            Text(
                text = stringResource(id = R.string.author_creditor) + " $it",
                style = adaptiveTextCreator,
                color = MaterialTheme.colors.background
            )
        }
    }
}

@Composable
private fun SongCardBottomContent(modifier: Modifier = Modifier, songObject: SongModel?) {
    val adaptiveTextName = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        letterSpacing = 0.25.sp,
        background = MaterialTheme.colors.onBackground
    )

    val adaptiveTextCreator = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        letterSpacing = 0.25.sp,
        background = MaterialTheme.colors.onBackground
    )

    BaseSongCardBottomContent(modifier = modifier) {

        songObject?.name?.let {
            Text(
                modifier = Modifier.padding(bottom = dimensionResource(id = R.dimen.NusicDimenX1)),
                text = it,
                style = adaptiveTextName,
                color = MaterialTheme.colors.background
            )
        }
        songObject?.artist?.let {
            Text(
                text = stringResource(id = R.string.author_creditor) + " $it",
                style = adaptiveTextCreator,
                color = MaterialTheme.colors.background
            )
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
private fun SongCardErrorOverlay(
    modifier: Modifier = Modifier,
    onRetry: () -> Unit,
    onCancel: () -> Unit,
    errorMessage: String,
    cancelAvailable: Boolean
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

                SongCardOverlayStandardButton(
                    painter = painterResource(id = R.drawable.nusic_replay_icon),
                    contentDescription = stringResource(id = R.string.restart_button_content_description),
                    onClick = onRetry
                )

                if (cancelAvailable) {
                    SongCardOverlayStandardButton(
                        painter = painterResource(id = R.drawable.nusic_cancel_icon),
                        contentDescription = stringResource(id = R.string.cancel_button_content_description),
                        onClick = onCancel
                    )
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
                SongCardOverlayStandardButton(
                    painter = painterResource(id = R.drawable.nusic_play_button),
                    contentDescription = stringResource(id = R.string.play_button_content_description),
                    onClick = onPlay
                )

                SongCardOverlayStandardButton(
                    painter = painterResource(id = R.drawable.nusic_replay_icon),
                    contentDescription = stringResource(id = R.string.restart_button_content_description),
                    onClick = onRestart
                )
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
            SongCardOverlayStandardButton(
                painter = painterResource(id = R.drawable.nusic_replay_icon),
                contentDescription = stringResource(id = R.string.restart_button_content_description),
                onClick = onReplay
            )
        }
    }
}

@Composable
private fun SongCardOverlayStandardButton(
    painter: Painter, contentDescription: String, onClick: () -> Unit
) {
    IconButton(onClick = { onClick() }) {
        Icon(
            modifier = Modifier
                .size(dimensionResource(id = R.dimen.NusicDimenX8))
                .padding(
                    dimensionResource(id = R.dimen.NusicDimenX1)
                ), painter = painter, contentDescription = contentDescription, tint = colorResource(
                id = R.color.pause_play_color
            )
        )
    }
}
