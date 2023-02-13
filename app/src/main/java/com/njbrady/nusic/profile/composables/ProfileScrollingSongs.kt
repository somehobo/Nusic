package com.njbrady.nusic.profile.composables

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.items
import com.njbrady.nusic.MainViewModel
import com.njbrady.nusic.home.SongCard
import com.njbrady.nusic.profile.requests.Type
import com.njbrady.nusic.utils.composables.NavigationTopAppBar
import com.njbrady.nusic.R
import com.njbrady.nusic.home.utils.SongCardState


@Composable
fun ProfileScrollingSongs(
    mainViewModel: MainViewModel, navController: NavController, selectedSongIndex: Int, type: Type
) {
    val displayedSongs = if (type == Type.Liked) mainViewModel.likedSongs.collectAsLazyPagingItems()
    else mainViewModel.createdSongs.collectAsLazyPagingItems()

    Scaffold(topBar = {
        NavigationTopAppBar(
            navController = navController,
            title = if (type == Type.Liked) stringResource(R.string.liked_songs_header) else stringResource(
                R.string.created_songs_header
            ),
            onBackClick = {
                mainViewModel.currentlyPlayingSong?.quietPauseWhenReady()
                mainViewModel.currentlyPlayingSong = null
            }
        )
    }) { paddingValues ->
        ScrollingSongList(
            paddingValues = paddingValues,
            displayedSongs = displayedSongs,
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
    selectedSongIndex: Int,
    mainViewModel: MainViewModel
) {
    val lazyListState = rememberLazyListState()

    LaunchedEffect(lazyListState) {
        snapshotFlow { lazyListState.layoutInfo }.collect { layoutInfo ->
            layoutInfo.visibleItemsInfo.forEach {
                val visibleSongCard = displayedSongs[it.index]
                if (it.offset == 0 && visibleSongCard != mainViewModel.currentlyPlayingSong) {
                    mainViewModel.currentlyPlayingSong?.pauseWhenReady()
                    visibleSongCard?.resetForcePause()
                    visibleSongCard?.playIfFirst()
                    mainViewModel.currentlyPlayingSong = visibleSongCard
                }
            }
        }
    }

    LaunchedEffect(true) {
        lazyListState.scrollToItem(selectedSongIndex)
    }


    LazyColumn(
        modifier = Modifier.padding(paddingValues),
        state = lazyListState,
        flingBehavior = rememberSnapFlingBehavior(lazyListState = lazyListState),
    ) {
        items(displayedSongs) { songCardState ->
            songCardState?.let {
                val songCardStateState by songCardState.songCardStateState.collectAsState()
                val songCardStateError by songCardState.errorMessage.collectAsState()
                SongCard(
                    modifier = Modifier
                        .fillParentMaxSize()
                        .padding(dimensionResource(id = R.dimen.NusicDimenX1))
                        .clickable { songCardState.pause() },
                    songCardStateState = songCardStateState,
                    errorMessage = songCardStateError,
                    songObject = songCardState.songObject,
                    onRestart = { songCardState.restart() },
                    onResume = { songCardState.resume() },
                    onRetry = { songCardState.retry() },
                    cancelAvailable = false
                )
            }
        }
    }
}
