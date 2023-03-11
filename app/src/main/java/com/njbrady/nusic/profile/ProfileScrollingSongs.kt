package com.njbrady.nusic.profile

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemsIndexed
import com.njbrady.nusic.MainViewModel
import com.njbrady.nusic.R
import com.njbrady.nusic.home.utils.SongCardState
import com.njbrady.nusic.profile.requests.SongListType
import com.njbrady.nusic.utils.composables.ItemImpression
import com.njbrady.nusic.utils.composables.NavigationTopAppBar
import com.njbrady.nusic.utils.composables.SongCard


@Composable
fun ProfileScrollingSongs(
    mainViewModel: MainViewModel,
    navController: NavController,
    songListType: SongListType
) {
    val displayedSongs =
        if (songListType == SongListType.Liked) mainViewModel.likedSongs.collectAsLazyPagingItems()
        else mainViewModel.createdSongs.collectAsLazyPagingItems()

    val prependedDisplayedSongs by if (songListType == SongListType.Liked) mainViewModel.prependedLikedSongs.collectAsState()
    else mainViewModel.prependedCreatedSongs.collectAsState()
    val selectedSongIndex = mainViewModel.selectedSongIndex

    Scaffold(topBar = {
        NavigationTopAppBar(
            navController = navController,
            title = if (songListType == SongListType.Liked) stringResource(R.string.liked_songs_header) else stringResource(
                R.string.created_songs_header
            ),
        )
    }) { paddingValues ->
        ScrollingSongList(
            paddingValues = paddingValues,
            displayedSongs = displayedSongs,
            prependedDisplayedSongs = prependedDisplayedSongs,
            selectedSongIndex = selectedSongIndex,
            mainViewModel = mainViewModel
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ScrollingSongList(
    paddingValues: PaddingValues,
    displayedSongs: LazyPagingItems<SongCardState>,
    prependedDisplayedSongs: List<Pair<SongCardState, Boolean>>,
    selectedSongIndex: Int,
    mainViewModel: MainViewModel
) {
    val lazyListState = rememberLazyListState()

    LaunchedEffect(Unit) {
        lazyListState.scrollToItem(selectedSongIndex)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        state = lazyListState,
        flingBehavior = rememberSnapFlingBehavior(lazyListState = lazyListState),
    ) {
        itemsIndexed(items = prependedDisplayedSongs) { index, pair ->
            val songCardState = pair.first
            val liked = pair.second
            if (liked) {
                songItem(
                    songCardState = songCardState,
                    currentlyPlayingSong = mainViewModel.currentlyPlayingSong,
                    lazyListState = lazyListState,
                    setCurrentlyPlayingSong = { songCardState, index ->
                        mainViewModel.setCurrentPlayingScrollingSong(songCardState, index)
                    },
                    index = index
                )
            }
        }

        itemsIndexed(items = displayedSongs) { index, songCardState ->
            songCardState?.let {
                songItem(songCardState = songCardState,
                    currentlyPlayingSong = mainViewModel.currentlyPlayingSong,
                    index = index + prependedDisplayedSongs.size,
                    lazyListState = lazyListState,
                    setCurrentlyPlayingSong = { songCardState, index ->
                        mainViewModel.setCurrentPlayingScrollingSong(songCardState, index)

                    })
            }
        }
    }
}

@Composable
private fun songItem(
    songCardState: SongCardState,
    currentlyPlayingSong: SongCardState?,
    lazyListState: LazyListState,
    setCurrentlyPlayingSong: (SongCardState, Int) -> Unit,
    index: Int
) {
    ItemImpression(index = index, lazyListState = lazyListState, onItemViewed = {
        if (songCardState != currentlyPlayingSong) {
            currentlyPlayingSong?.pauseWhenReady()
            songCardState.replayFromScroll()
            setCurrentlyPlayingSong(songCardState, index)
        }
    })
    with(songCardState) {
        val songCardStateState by songCardStateState.collectAsState()
        val songCardStateError by errorMessage.collectAsState()

        SongCard(
            modifier = Modifier
                .fillMaxSize()
                .padding(dimensionResource(id = R.dimen.NusicDimenX1))
                .clickable { pause() },
            songCardStateState = songCardStateState,
            errorMessage = songCardStateError,
            songObject = songObject,
            onRestart = { restart() },
            onResume = { resume() },
            onRetry = { retry() },
            cancelAvailable = false
        )
    }
}
