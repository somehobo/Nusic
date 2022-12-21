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
import com.njbrady.nusic.home.utils.Direction
import com.njbrady.nusic.home.utils.rememberSwipeableCardState
import com.njbrady.nusic.home.utils.swipableCard
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
    Scaffold(
        modifier = Modifier.windowInsetsPadding(
            WindowInsets.navigationBars.only(WindowInsetsSides.End)
        ),
        backgroundColor = MaterialTheme.colors.background
    ) { paddingValues ->
        SongStack(
            songList = homeState.songList,
            onLikeAction = onLikeAction,
            modifier = Modifier,
            paddingValues = paddingValues
        )
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterialApi::class)
@Composable
private fun SongStack(
    songList: SnapshotStateList<Song>,
    onLikeAction: (Song, Boolean) -> Unit,
    modifier: Modifier,
    paddingValues: PaddingValues = PaddingValues()
) {
    remember {
        songList
    }
    val states = songList.reversed()
        .map { it to rememberSwipeableCardState() }
    Box(
        modifier = modifier.fillMaxSize(),
    ) {

        states.forEach { (song, state) ->
            if (state.swipedDirection == null) {
                SongCard(
                    song = song,
                    modifier = Modifier
                        .fillMaxSize()
                        .swipableCard(
                            state = state,
                            blockedDirections = listOf(Direction.Down),
                            onSwiped = { dir ->
                                val liked = dir == Direction.Right
                                onLikeAction(song, liked)
                            },
                        ),
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun SongCard(
    song: Song,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .padding(horizontal = 8.dp, vertical = 8.dp)

    ) {
        Row {
            Column {
                Text(text = song.songName)
                Text(text = song.artistName)
            }
        }
    }

}



