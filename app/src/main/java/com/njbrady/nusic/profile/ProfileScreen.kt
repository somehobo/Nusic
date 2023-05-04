package com.njbrady.nusic


import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Settings
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
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemsIndexed
import com.njbrady.nusic.profile.ProfileScrollingSongs
import com.njbrady.nusic.profile.ProfileViewModel
import com.njbrady.nusic.profile.composables.*
import com.njbrady.nusic.profile.requests.SongListType
import com.njbrady.nusic.profile.utils.SongListFurtherCommunicatedState
import com.njbrady.nusic.profile.utils.SongListInitialCommunicatedState
import com.njbrady.nusic.ui.theme.NusicTheme
import com.njbrady.nusic.upload.UploadScreen
import com.njbrady.nusic.upload.UploadScreenViewModel

@Composable
fun ProfileScreen(
    mainViewModel: ProfileViewModel,
    uploadScreenViewModel: UploadScreenViewModel,
) {
    val profileNavController = rememberNavController()
    ProfileScrenNavigation(
        profileViewModel = mainViewModel,
        profileNavController = profileNavController,
        uploadScreenViewModel = uploadScreenViewModel,
    )
}

@Composable
private fun ProfileScrenNavigation(
    profileViewModel: ProfileViewModel,
    uploadScreenViewModel: UploadScreenViewModel,
    profileNavController: NavHostController,
) {
    var currentlySelected by remember {
        mutableStateOf(SongListType.Liked)
    }

    Scaffold { paddingValues ->
        NavHost(
            navController = profileNavController,
            startDestination = ProfileScreens.ProfileHome.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(ProfileScreens.ProfileHome.route) {
                profileViewModel.pauseAndReset()
                uploadScreenViewModel.pauseWhenReady()
                ProfileScreenContent(profileViewModel = profileViewModel,
                    currentlySelected = currentlySelected,
                    onFilter = { newFilter -> currentlySelected = newFilter },
                    onSelected = { index ->
                        profileViewModel.selectedSongIndex = index
                        profileNavController.navigate(ProfileScreens.LCSongs.route)
                    },
                    onUploadHit = {
                        profileNavController.navigate(ProfileScreens.Upload.route)
                    })
            }
            composable(ProfileScreens.LCSongs.route) {
                uploadScreenViewModel.pauseWhenReady()
                ProfileScrollingSongs(
                    profileViewModel = profileViewModel, songListType = currentlySelected
                )
            }
            composable(ProfileScreens.Upload.route) {
                UploadScreen(
                    uploadScreenViewModel = uploadScreenViewModel,
                )
            }
        }
    }

    LocalNavController.current.addOnDestinationChangedListener { _, destination, _ ->
        if (destination.route != Screen.Profile.route) {
            if (profileNavController.currentDestination?.route != ProfileScreens.ProfileHome.route) {
                profileNavController.navigate(
                    route = ProfileScreens.ProfileHome.route
                )
            }
        }

    }
}

@OptIn(
    ExperimentalFoundationApi::class, ExperimentalMaterialApi::class
)
@Composable
fun ProfileScreenContent(
    profileViewModel: ProfileViewModel,
    currentlySelected: SongListType,
    onFilter: (SongListType) -> Unit,
    onSelected: (Int) -> Unit,
    onUploadHit: () -> Unit
) {

    val likedSongs = profileViewModel.likedSongs.collectAsLazyPagingItems()
    val createdSongs = profileViewModel.createdSongs.collectAsLazyPagingItems()
    val prependedLikedSongs by profileViewModel.prependedLikedSongs.collectAsState()
    val prependedCreatedSongs by profileViewModel.prependedCreatedSongs.collectAsState()
    val displayedPrependedSongs =
        if (currentlySelected == SongListType.Liked) prependedLikedSongs else prependedCreatedSongs
    val displayedSongs = if (currentlySelected == SongListType.Liked) likedSongs else createdSongs
    val refreshing by profileViewModel.refreshingProfile.collectAsState()
    val bioState by profileViewModel.bioState.collectAsState()
    val bio by profileViewModel.bio.collectAsState()

    val pullRefreshState = rememberPullRefreshState(refreshing = refreshing, onRefresh = {
        likedSongs.refresh()
        createdSongs.refresh()
        profileViewModel.refreshProfile()
        profileViewModel.setRefresh(false)
    })

    Scaffold(topBar = { ProfileScreenHeader(profileViewModel, onUploadHit) }) { paddingValues ->
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
                        ProfilePhotoComposable(profileViewModel.profilePhoto)
                    }
                }

                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = dimensionResource(id = R.dimen.NusicDimenX3)),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        ProfileUsername(
                            username = profileViewModel.userName
                        )
                    }
                }

                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = dimensionResource(id = R.dimen.NusicDimenX3)),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        ProfileBio(
                            modifier = Modifier
                                .padding(horizontal = dimensionResource(id = R.dimen.NusicDimenX2)),
                            bio = bio,
                            state = bioState,
                            uploadedBio = profileViewModel.uploadedBio,
                            onValueChanged = { new ->  profileViewModel.updateTempBio(new) },
                            onDone = { profileViewModel.uploadCurrentBio() },
                            onFocusChanged = { profileViewModel.resetBio() },
                            onFocusing = { profileViewModel.onFocusing() }
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
                        item.first.songModel.let {
                            MusicElement(songModel = it, onSelected = onSelected, index = index)
                            Divider()
                        }
                    }
                }

                itemsIndexed(items = displayedSongs) { index, item ->
                    val canShow = displayedPrependedSongs.find {
                        it.second && it.first.songModel.songId == item?.songModel?.songId
                    }
                    if (canShow == null) {
                        item?.songModel?.let {
                            MusicElement(songModel = it, onSelected = onSelected, index = index)
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
private fun ProfileScreenHeader(profileViewModel: ProfileViewModel, onUploadHit: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    TopAppBar(backgroundColor = Color.Transparent, elevation = 0.dp, title = {}, actions = {
        IconButton(
            onClick = { onUploadHit() },
            modifier = Modifier
                .size(dimensionResource(id = R.dimen.NusicDimenX5))
                .padding(
                    end = dimensionResource(id = R.dimen.NusicDimenX1)
                )
        ) {
            Icon(
                modifier = Modifier.size(dimensionResource(id = R.dimen.NusicDimenX5)),
                imageVector = Icons.Outlined.Add,
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
                    modifier = Modifier.size(dimensionResource(id = R.dimen.NusicDimenX5)),
                    imageVector = Icons.Outlined.Settings,
                    contentDescription = stringResource(R.string.settings_button)
                )
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                DropdownMenuItem(onClick = { profileViewModel.logout() }) {
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
    NusicTheme {}
}
