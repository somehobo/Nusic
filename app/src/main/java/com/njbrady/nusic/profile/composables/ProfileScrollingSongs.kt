package com.njbrady.nusic.profile.composables

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.navigation.NavController
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.items
import com.njbrady.nusic.MainViewModel
import com.njbrady.nusic.home.SongCard
import com.njbrady.nusic.home.responseObjects.SongObject
import com.njbrady.nusic.home.utils.SongCardStateStates
import com.njbrady.nusic.profile.requests.Type
import com.njbrady.nusic.utils.composables.NavigationTopAppBar
import com.njbrady.nusic.R

@Composable
fun ProfileScrollingSongs(
    mainViewModel: MainViewModel,
    navController: NavController,
    selectedSong: SongObject?,
    type: Type
) {
    val displayedSongs = if (type == Type.Liked) mainViewModel.likedSongs.collectAsLazyPagingItems()
    else mainViewModel.createdSongs.collectAsLazyPagingItems()

    Scaffold(topBar = {
        NavigationTopAppBar(
            navController = navController,
            title = if (type == Type.Liked) "Liked Songs" else "Created Songs"
        )
    }) { paddingValues ->
        ScrollingSongList(
            paddingValues = paddingValues,
            displayedSongs = displayedSongs,
            selectedSong = selectedSong
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ScrollingSongList(
    paddingValues: PaddingValues,
    displayedSongs: LazyPagingItems<SongObject>,
    selectedSong: SongObject?
) {
    val lazyListState = rememberLazyListState()
    LazyColumn(
        modifier = Modifier.padding(paddingValues),
        state = lazyListState,
        flingBehavior = rememberSnapFlingBehavior(lazyListState = lazyListState)
    ) {
        items(displayedSongs) { songObject ->
            SongCard(
                modifier = Modifier
                    .fillParentMaxSize()
                    .padding(dimensionResource(id = R.dimen.NusicDimenX1)),
                songCardStateState = SongCardStateStates.Playing,
                errorMessage = "",
                songObject = songObject
            )
        }
    }
}
