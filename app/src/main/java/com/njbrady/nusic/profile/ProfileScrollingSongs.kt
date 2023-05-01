package com.njbrady.nusic.profile

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Box
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
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemsIndexed
import com.njbrady.nusic.LocalNavController
import com.njbrady.nusic.ProfileViewModel
import com.njbrady.nusic.R
import com.njbrady.nusic.profile.requests.SongListType
import com.njbrady.nusic.upload.PlayerState
import com.njbrady.nusic.utils.SongPlayerWrapper
import com.njbrady.nusic.utils.composables.ItemImpression
import com.njbrady.nusic.utils.composables.NavigationTopAppBar
import com.njbrady.nusic.utils.composables.SongCard


@Composable
fun ProfileScrollingSongs(
    profileViewModel: ProfileViewModel, songListType: SongListType
) {
    val displayedSongs =
        if (songListType == SongListType.Liked) profileViewModel.likedSongs.collectAsLazyPagingItems()
        else profileViewModel.createdSongs.collectAsLazyPagingItems()

    val prependedDisplayedSongs by if (songListType == SongListType.Liked) profileViewModel.prependedLikedSongs.collectAsState()
    else profileViewModel.prependedCreatedSongs.collectAsState()
    val selectedSongIndex = profileViewModel.selectedSongIndex

    Scaffold(topBar = {
        NavigationTopAppBar(
            navController = LocalNavController.current,
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
            profileViewModel = profileViewModel
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ScrollingSongList(
    paddingValues: PaddingValues,
    displayedSongs: LazyPagingItems<SongPlayerWrapper>,
    prependedDisplayedSongs: List<Pair<SongPlayerWrapper, Boolean>>,
    selectedSongIndex: Int,
    profileViewModel: ProfileViewModel
) {
    val lazyListState = rememberLazyListState()

    val topSongState by profileViewModel.topSongState.collectAsState()
    val topSongErrorMessage by profileViewModel.topSongErrorMessage.collectAsState()
    val psd by profileViewModel.psd.collectAsState()

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
                Box(modifier = Modifier.fillParentMaxSize()) {
                    songItem(
                        songPlayerWrapper = songCardState,
                        currentlyPlayingSong = profileViewModel.currentlyPlayingSong,
                        lazyListState = lazyListState,
                        setCurrentlyPlayingSong = { songCardState, index ->
                            profileViewModel.setCurrentPlayingScrollingSong(songCardState, index)
                        },
                        index = index,
                        currentCardErrorMessage = topSongErrorMessage,
                        currentCardState = topSongState,
                        pauseCurrentSong = { profileViewModel.pauseCurrent() },
                        psd = psd
                    )
                }
            }
        }

        itemsIndexed(items = displayedSongs) { index, songCardState ->
            songCardState?.let {
                Box(modifier = Modifier.fillParentMaxSize()) {
                    songItem(songPlayerWrapper = songCardState,
                        currentlyPlayingSong = profileViewModel.currentlyPlayingSong,
                        index = index + prependedDisplayedSongs.size,
                        lazyListState = lazyListState,
                        setCurrentlyPlayingSong = { songPlayerWrapper, index ->
                            profileViewModel.setCurrentPlayingScrollingSong(songPlayerWrapper, index)
                        },
                        currentCardErrorMessage = topSongErrorMessage,
                        currentCardState = topSongState,
                        pauseCurrentSong = { profileViewModel.pauseCurrent() },
                        psd = psd
                    )
                }
            }
        }
    }
}

@Composable
private fun songItem(
    songPlayerWrapper: SongPlayerWrapper,
    currentlyPlayingSong: SongPlayerWrapper?,
    pauseCurrentSong: () -> Unit,
    lazyListState: LazyListState,
    setCurrentlyPlayingSong: (SongPlayerWrapper, Int) -> Unit,
    currentCardState: PlayerState,
    currentCardErrorMessage: String?,
    psd: FloatArray,
    index: Int
) {
    val songCardModifier = Modifier
        .fillMaxSize()
        .padding(dimensionResource(id = R.dimen.NusicDimenX1))
        .clickable { pauseCurrentSong() }

    ItemImpression(index = index, lazyListState = lazyListState, onItemViewed = {
        if (songPlayerWrapper != currentlyPlayingSong) {
            pauseCurrentSong()
            songPlayerWrapper.restart()
            setCurrentlyPlayingSong(songPlayerWrapper, index)
        }
    })
    with(songPlayerWrapper) {
        if (currentlyPlayingSong == songPlayerWrapper) {
            SongCard(
                modifier = songCardModifier,
                errorMessage = currentCardErrorMessage,
                playerState = currentCardState,
                songModel = songModel,
                onRestart = { restart() },
                onResume = { play() },
                onRetry = { reset() },
                cancelAvailable = false,
                psd = psd
            )
        } else {
            SongCard(
                modifier = songCardModifier,
                errorMessage = null,
                playerState = PlayerState.Playing,
                songModel = songModel,
                cancelAvailable = false
            )
        }
    }

}
