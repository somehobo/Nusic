package com.njbrady.nusic


import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.njbrady.nusic.home.data.HomeState
import com.njbrady.nusic.home.data.Song
import com.njbrady.nusic.ui.theme.NusicTheme


@Composable
fun HomeScreen(
    mainViewModel: MainViewModel
) {
    val homeState = remember {
        mainViewModel.homeState
    }

    HomeScreenContent(
        homeState = homeState,
        onLikeAction = { liked -> mainViewModel.likeSong(liked) }
    )
}

@Composable
private fun HomeScreenContent(
    homeState: HomeState,
    onLikeAction: (Boolean) -> Unit
) {
    Scaffold(
        modifier = Modifier.windowInsetsPadding(
            WindowInsets.navigationBars.only(WindowInsetsSides.End)
        ),
        backgroundColor = MaterialTheme.colors.background
    ) { paddingValues ->
        Column(verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxSize().padding(paddingValues.calculateBottomPadding())) {
            SongStack(
                homeState = homeState,
                onLikeAction = onLikeAction,
                paddingValues = paddingValues,
                modifier = Modifier.fillMaxSize(0.8f)
            )
            FeedbackButtons(onLikeAction = onLikeAction)
        }
    }
}

@Composable
private fun FeedbackButtons(
    onLikeAction: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
    ) {
        //dislike
        Button(
            modifier = Modifier.padding(horizontal = 8.dp),
            colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.error),
            onClick = { onLikeAction(false) }
        ) {
            Text(text = "Dislike")
        }
        Button(
            modifier = Modifier.padding(horizontal = 8.dp),
            colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.secondaryVariant),
            onClick = { onLikeAction(true) }) {
            Text(text = "Like")
        }
    }
}


@Composable
private fun SongStack(
    homeState: HomeState,
    onLikeAction: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    paddingValues: PaddingValues = PaddingValues()
) {


    Box(
        modifier = modifier,
    ) {
        for (song in homeState.songList) {
            SongCard(song = song)
//            val state = rememberSwipeableCardState()
//            if (state.swipedDirection == null) {
//                SongCard(
//                    song = song,
//                    modifier = Modifier
//                        .fillMaxSize()
//                        .swipableCard(
//                            state = state,
//                            blockedDirections = listOf(Direction.Down),
//                            onSwiped = { dir ->
//                                val liked = dir == Direction.Right
//                                onLikeAction(song, liked)
//                            },
//                        ),
//                )
//            }
        }
    }
}

@Composable
private fun SongCard(
    song: Song,
    modifier: Modifier = Modifier,
) {

    Card(
        modifier = modifier
            .padding(horizontal = 8.dp, vertical = 8.dp)
            .fillMaxSize()
    ) {
        Row {
            Column {
                Text(text = song.songName)
                Text(text = song.artistName)
            }
        }
    }

}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    NusicTheme {
        FeedbackButtons(onLikeAction = {})
    }
}



