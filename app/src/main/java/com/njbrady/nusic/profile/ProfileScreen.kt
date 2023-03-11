package com.njbrady.nusic


import androidx.annotation.StringRes
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.*
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemsIndexed
import com.njbrady.nusic.profile.ProfileScrollingSongs
import com.njbrady.nusic.profile.composables.*
import com.njbrady.nusic.profile.requests.SongListType
import com.njbrady.nusic.profile.utils.SongListFurtherCommunicatedState
import com.njbrady.nusic.profile.utils.SongListInitialCommunicatedState
import com.njbrady.nusic.ui.theme.NusicTheme


@Composable
fun ProfileScreen(mainViewModel: MainViewModel, navController: NavController) {
    val profileNavController = rememberNavController()
    ProfileScrenNavigation(
        mainViewModel = mainViewModel,
        profileNavController = profileNavController,
        mainNavController = navController
    )
}

@Composable
private fun ProfileScrenNavigation(
    mainViewModel: MainViewModel,
    profileNavController: NavHostController,
    mainNavController: NavController
) {
    var currentlySelected by remember {
        mutableStateOf(SongListType.Liked)
    }

    Scaffold { paddingValues ->
        NavHost(
            navController = profileNavController,
            startDestination = ProfileScreens.Profile.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(ProfileScreens.Profile.route) {
                mainViewModel.pauseAndReset()
                ProfileScreenContent(mainViewModel = mainViewModel,
                    currentlySelected = currentlySelected,
                    onFilter = { newFilter -> currentlySelected = newFilter },
                    onSelected = { index ->
                        mainViewModel.selectedSongIndex = index
                        profileNavController.navigate(ProfileScreens.LCSongs.route)
                    })
            }
            composable(ProfileScreens.LCSongs.route) {
                ProfileScrollingSongs(
                    mainViewModel = mainViewModel,
                    navController = profileNavController,
                    songListType = currentlySelected
                )
            }
        }
    }

    mainNavController.addOnDestinationChangedListener { _, destination, _ ->
        if (destination.route != Screen.Profile.route) {
            if (profileNavController.currentDestination?.route != ProfileScreens.Profile.route) profileNavController.navigate(
                route = ProfileScreens.Profile.route
            )
        }

    }
}

@OptIn(
    ExperimentalFoundationApi::class, ExperimentalMaterialApi::class
)
@Composable
private fun ProfileScreenContent(
    mainViewModel: MainViewModel,
    currentlySelected: SongListType,
    onFilter: (SongListType) -> Unit,
    onSelected: (Int) -> Unit
) {

    val likedSongs = mainViewModel.likedSongs.collectAsLazyPagingItems()
    val createdSongs = mainViewModel.createdSongs.collectAsLazyPagingItems()
    val prependedLikedSongs by mainViewModel.prependedLikedSongs.collectAsState()
    val prependedCreatedSongs by mainViewModel.prependedCreatedSongs.collectAsState()
    val displayedPrependedSongs =
        if (currentlySelected == SongListType.Liked) prependedLikedSongs else prependedCreatedSongs
    val displayedSongs = if (currentlySelected == SongListType.Liked) likedSongs else createdSongs
    val refreshing by mainViewModel.refreshingProfile.collectAsState()

    val pullRefreshState = rememberPullRefreshState(refreshing = refreshing, onRefresh = {
        likedSongs.refresh()
        createdSongs.refresh()
        mainViewModel.refreshProfile()
        mainViewModel.setRefresh(false)
    })

    Scaffold(topBar = { ProfileScreenHeader(mainViewModel) }) { paddingValues ->
        Box(
            modifier = Modifier
                .pullRefresh(pullRefreshState, enabled = true)
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = dimensionResource(id = R.dimen.NusicDimenX4)),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        ProfilePhotoComposable(mainViewModel.profilePhoto)
                    }
                }

                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = dimensionResource(id = R.dimen.NusicDimenX3))
                    ) {
                        ProfileUsername(
                            username = mainViewModel.username ?: ""
                        )
                    }
                }

                stickyHeader {
                    MusicSelectionTab(modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            bottom = dimensionResource(
                                id = R.dimen.NusicDimenX2
                            )
                        ), currentlySelected = currentlySelected, onFilter = { newFilter ->
                        if (newFilter != currentlySelected) {
                            onFilter(newFilter)
                        }
                    })
                }

                itemsIndexed(items = displayedPrependedSongs) { index, item ->
                    if (item.second) {
                        item.first.songObject?.let {
                            MusicElement(songObject = it, onSelected = onSelected, index = index)
                            Divider()
                        }
                    }
                }

                itemsIndexed(items = displayedSongs) { index, item ->
                    val canShow = displayedPrependedSongs.find {
                        it.second && it.first.songObject?.songId == item?.songObject?.songId
                    }
                    if (canShow == null) {
                        item?.songObject?.let {
                            MusicElement(songObject = it, onSelected = onSelected, index = index)
                            Divider()
                        }
                    }
                }

                item {
                    SongListInitialCommunicatedState(loadState = displayedSongs.loadState)
                }

                item {
                    SongListFurtherCommunicatedState(loadState = displayedSongs.loadState)
                }
            }
            PullRefreshIndicator(refreshing, pullRefreshState, Modifier.align(Alignment.TopCenter))
        }
    }
}




@Composable
private fun ProfileScreenHeader(mainViewModel: MainViewModel) {
    var expanded by remember { mutableStateOf(false) }

    TopAppBar(modifier = Modifier.fillMaxWidth(),
        backgroundColor = Color.Transparent,
        elevation = 0.dp,
        title = {},
        actions = {
            IconButton(
                onClick = { /*on upload*/ },
                modifier = Modifier
                    .size(dimensionResource(id = R.dimen.NusicDimenX4))
                    .padding(
                        end = dimensionResource(id = R.dimen.NusicDimenX1)
                    )
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.nusic_upload_icon),
                    contentDescription = stringResource(R.string.upload_button)
                )
            }
            Box(
                modifier = Modifier.wrapContentSize(Alignment.TopStart)
            ) {
                IconButton(
                    onClick = { expanded = true },
                    modifier = Modifier
                        .size(dimensionResource(id = R.dimen.NusicDimenX5))
                        .padding(
                            end = dimensionResource(id = R.dimen.NusicDimenX1)
                        )
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.nusic_settings_icon),
                        contentDescription = stringResource(R.string.settings_button)
                    )
                }
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    DropdownMenuItem(onClick = { mainViewModel.logout() }) {
                        Text(stringResource(R.string.logout), color = MaterialTheme.colors.error)
                        Icon(
                            modifier = Modifier
                                .size(dimensionResource(id = R.dimen.NusicDimenX6))
                                .padding(
                                    start = dimensionResource(
                                        id = R.dimen.NusicDimenX1
                                    )
                                ),
                            painter = painterResource(id = R.drawable.nusic_portal_exit),
                            tint = MaterialTheme.colors.error,
                            contentDescription = stringResource(R.string.logout)
                        )
                    }
                }
            }
        })
}


@Composable
@Preview(showBackground = true)
private fun viewer() {
    NusicTheme {
    }
}

sealed class ProfileScreens(val route: String, @StringRes val resourceId: Int) {
    object Profile : ProfileScreens("ProfileScreen", R.string.profile_screen)
    object LCSongs : ProfileScreens("LCSongs", R.string.scrolling_songs_screen)
}