package com.njbrady.nusic

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.njbrady.nusic.home.data.HomeState
import com.njbrady.nusic.home.data.Song
import java.util.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.layout.LazyLayout
import androidx.compose.foundation.lazy.layout.LazyLayoutItemProvider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.input.pointer.consumeAllChanges
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import com.njbrady.nusic.home.utils.Direction
import com.njbrady.nusic.home.utils.rememberSwipeableCardState
import com.njbrady.nusic.home.utils.swipableCard
import kotlinx.coroutines.launch
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
            homeState = homeState,
            onLikeAction = onLikeAction,
            modifier = Modifier,
            paddingValues = paddingValues
        )
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterialApi::class)
@Composable
private fun SongStack(
    homeState: HomeState,
    onLikeAction: (Song, Boolean) -> Unit,
    modifier: Modifier,
    paddingValues: PaddingValues = PaddingValues()
) {

    LazyColumn(content = )
    Box(
        modifier = modifier.fillMaxSize(),
    ) {
        for(song in homeState.songList) {
            val state = rememberSwipeableCardState()
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



