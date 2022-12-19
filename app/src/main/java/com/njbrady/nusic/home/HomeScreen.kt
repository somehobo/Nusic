package com.njbrady.nusic

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.njbrady.nusic.home.data.HomeState
import com.njbrady.nusic.home.data.Song
import java.util.*

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
        SongStack(homeState = homeState, onLikeAction = onLikeAction, modifier = Modifier, paddingValues = paddingValues)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SongStack(
    homeState: HomeState,
    onLikeAction: (Song, Boolean) -> Unit,
    modifier: Modifier,
    paddingValues: PaddingValues = PaddingValues()
) {
    Box(
        modifier = modifier.fillMaxSize(),
    ) {
        for (song in homeState.songList) {
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
    Card(
        modifier = Modifier
            .padding(horizontal = 8.dp, vertical = 8.dp)
            .fillMaxSize(),
        onClick = { onLikeAction }
    ) {
        Row {
            Column {
                Text(text = song.songName)
                Text(text = song.artistName)
            }
        }
    }
}
