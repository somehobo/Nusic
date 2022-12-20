package com.njbrady.nusic

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.njbrady.nusic.home.data.HomeState
import com.njbrady.nusic.home.data.Song
import java.util.*
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.*
import androidx.compose.ui.input.pointer.consumeAllChanges
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import kotlin.math.roundToInt


@Composable
fun HomeScreen(
    mainViewModel: MainViewModel
) {
    val homeState = remember {
        mainViewModel.homeState
    }

    HomeScreenContent(
        homeState = homeState,
        onLikeAction = { song, liked -> mainViewModel.likeSong(song, liked) }
    )
}

@Composable
private fun HomeScreenContent(
    homeState: HomeState,
    onLikeAction: (Song, Boolean) -> Unit
) {
    val songListState = homeState.songList
    Scaffold(
        modifier = Modifier.windowInsetsPadding(
            WindowInsets.navigationBars.only(WindowInsetsSides.End)
        ),
        backgroundColor = MaterialTheme.colors.background
    ) { paddingValues ->
        SongStack(
            songListState = songListState,
            onLikeAction = onLikeAction,
            modifier = Modifier,
            paddingValues = paddingValues
        )
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterialApi::class)
@Composable
private fun SongStack(
    songListState: SnapshotStateList<Song>,
    onLikeAction: (Song, Boolean) -> Unit,
    modifier: Modifier,
    paddingValues: PaddingValues = PaddingValues()
) {
    Box(
        modifier = modifier.fillMaxSize(),
    ) {

        songListState.forEach { song ->
            SongCard(song = song, onLikeAction = onLikeAction)
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun SongCard(
    song: Song,
    onLikeAction: (Song, Boolean) -> Unit
) {
    val swipeableState = rememberSwipeableState(0)
    val anchors = mapOf(0f to 0, sizePx to 1)
    Card(
        modifier = Modifier
            .padding(horizontal = 8.dp, vertical = 8.dp)
            .fillMaxSize()
            .swipeable(
                state = swipeableState,

            )
    ) {
        Row {
            Column {
                Text(text = song.songName)
                Text(text = song.artistName)
            }
        }
    }
}
}

