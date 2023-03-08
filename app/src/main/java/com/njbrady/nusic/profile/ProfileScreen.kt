package com.njbrady.nusic


import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
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
import coil.compose.SubcomposeAsyncImage
import com.njbrady.nusic.home.model.SongModel
import com.njbrady.nusic.profile.composables.ProfileScrollingSongs
import com.njbrady.nusic.profile.requests.Type
import com.njbrady.nusic.profile.utils.ProfilePhoto
import com.njbrady.nusic.profile.utils.ProfilePhotoState
import com.njbrady.nusic.profile.utils.SongListFurtherCommunicatedState
import com.njbrady.nusic.profile.utils.SongListInitialCommunicatedState
import com.njbrady.nusic.ui.theme.NusicTheme
import com.njbrady.nusic.utils.glowLoad
import com.njbrady.nusic.utils.shimmerBackground


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
    var selectedSong = 0
    var currentlySelected by remember {
        mutableStateOf(Type.Liked)
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
                    onSelected = { songObject ->
                        selectedSong = songObject
                        profileNavController.navigate(ProfileScreens.LCSongs.route)
                    })
            }
            composable(ProfileScreens.LCSongs.route) {
                ProfileScrollingSongs(
                    mainViewModel = mainViewModel,
                    navController = profileNavController,
                    selectedSongIndex = selectedSong,
                    type = currentlySelected
                )
            }
        }
    }

    mainNavController.addOnDestinationChangedListener { _, destination, _ ->
        if (destination.route != Screen.Profile.route) {
            if (profileNavController.currentDestination?.route != ProfileScreens.Profile.route)
                profileNavController.navigate(route = ProfileScreens.Profile.route)
        }

    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalAnimationApi::class,
    ExperimentalMaterialApi::class
)
@Composable
private fun ProfileScreenContent(
    mainViewModel: MainViewModel,
    currentlySelected: Type,
    onFilter: (Type) -> Unit,
    onSelected: (Int) -> Unit
) {

    val likedSongs = mainViewModel.likedSongs.collectAsLazyPagingItems()
    val createdSongs = mainViewModel.createdSongs.collectAsLazyPagingItems()
    val prependedLikedSongs by mainViewModel.prependedLikedSongs.collectAsState()
    val prependedCreatedSongs by mainViewModel.prependedCreatedSongs.collectAsState()
    val displayedPrependedSongs = if (currentlySelected == Type.Liked) prependedLikedSongs else prependedCreatedSongs
    val displayedSongs = if (currentlySelected == Type.Liked) likedSongs else createdSongs
    val refreshing by mainViewModel.refreshingProfile.collectAsState()
    val pullRefreshState = rememberPullRefreshState(refreshing = refreshing, onRefresh = {
//        mainViewModel.setRefresh(true)
        likedSongs.refresh()
        createdSongs.refresh()
        mainViewModel.refreshProfile()
        mainViewModel.setRefresh(false)
    })
    Scaffold(topBar = { ProfileScreenHeader(mainViewModel) }) { paddingValues ->
        Box(modifier = Modifier
            .pullRefresh(pullRefreshState, enabled = true)
            .fillMaxSize()
            .padding(paddingValues)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
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
                    if(item.second) {
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
private fun MusicElement(songObject: SongModel, onSelected: (Int) -> Unit, index: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelected(index) },
        verticalAlignment = Alignment.CenterVertically
    ) {
        SongPreviewPicture(
            modifier = Modifier
                .padding(dimensionResource(id = R.dimen.NusicDimenX1))
                .size(dimensionResource(id = R.dimen.NusicDimenX5))
                .clip(
                    RoundedCornerShape(dimensionResource(id = R.dimen.NusicDimenX1))
                ), songObject = songObject
        )

        SongNameWithArtist(
            name = songObject.name, artist = songObject.artist
        )
    }
}

@Composable
private fun SongNameWithArtist(name: String?, artist: String?) {
    Column {
        name?.let {
            Text(
                modifier = Modifier.padding(
                    start = dimensionResource(id = R.dimen.NusicDimenX1),
                    top = dimensionResource(id = R.dimen.NusicDimenX1)
                ), text = it
            )
        }

        artist?.let {
            Text(
                modifier = Modifier.padding(
                    start = dimensionResource(id = R.dimen.NusicDimenX1),
                    bottom = dimensionResource(id = R.dimen.NusicDimenX1)
                ), text = "- $it", style = MaterialTheme.typography.caption
            )
        }
    }
}


@Composable
private fun SongPreviewPicture(modifier: Modifier = Modifier, songObject: SongModel) {
    Box(
        modifier = modifier
    ) {
        if (songObject.imageUrl != null) {
            SubcomposeAsyncImage(
                modifier = Modifier.fillMaxSize(),
                model = songObject.imageUrl,
                loading = {
                    Box(
                        modifier = Modifier
                            .background(colorResource(id = R.color.card_overlay))
                            .fillMaxSize()
                            .shimmerBackground(),
                    )
                },
                contentScale = ContentScale.Crop,
                contentDescription = stringResource(R.string.profile_image),
            )
        }
    }
}

@Composable
private fun MusicSelectionTab(
    modifier: Modifier = Modifier, currentlySelected: Type, onFilter: (Type) -> Unit
) {
    TabRow(modifier = modifier,
        selectedTabIndex = currentlySelected.ordinal,
        backgroundColor = MaterialTheme.colors.background,
        indicator = { tabPositions: List<TabPosition> ->
            Box(
                Modifier
                    .tabIndicatorOffset(tabPositions[currentlySelected.ordinal])
                    .fillMaxSize()
            ) {
                Divider(
                    modifier = Modifier.align(Alignment.BottomCenter),
                    color = MaterialTheme.colors.onBackground
                )
            }
        },
        divider = { }) {
        Type.values().forEachIndexed() { index, type ->
            val selected = index == currentlySelected.ordinal
            Tab(modifier = Modifier, selected = selected, onClick = {
                onFilter(type)
            }) {
                Text(
                    modifier = Modifier.padding(
                        vertical = dimensionResource(id = R.dimen.NusicDimenX1),
                        horizontal = dimensionResource(id = R.dimen.NusicDimenX2)
                    ), text = type.name
                )
            }
        }
    }

}

@Composable
private fun ProfilePhotoComposable(profilePhoto: ProfilePhoto) {
    val profilePhotoState by profilePhoto.profilePhotoState.collectAsState()
    val photoUrl by profilePhoto.photoUrl.collectAsState()
    val localContext = LocalContext.current
    val selectImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { uri: Uri? ->
        if (uri != null) {
            profilePhoto.setImage(uri, localContext)
        }
    }
    Box(modifier = Modifier
        .clip(shape = CircleShape)
        .clickable {
            if (profilePhotoState != ProfilePhotoState.SuccessPending)
                selectImageLauncher.launch("image/*")
        }
        .size(dimensionResource(id = R.dimen.ProfileImageDimen))) {
        photoUrl?.let {
            SubcomposeAsyncImage(
                modifier = Modifier.fillMaxSize(),
                model = it,
                loading = {
                    Box(
                        modifier = Modifier.background(colorResource(id = R.color.card_overlay)),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(dimensionResource(id = R.dimen.NusicDimenX8)))
                    }
                },
                contentDescription = stringResource(id = R.string.profile_image),
                contentScale = ContentScale.Crop
            )
            if (profilePhotoState == ProfilePhotoState.SuccessPending) {
                Box(modifier = Modifier
                    .fillMaxSize()
                    .glowLoad())
            }
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
//        MusicElement(songObject = SongObject(name = "test", artist = "ArtistTest"))
    }
}

sealed class ProfileScreens(val route: String, @StringRes val resourceId: Int) {
    object Profile : ProfileScreens("ProfileScreen", R.string.profile_screen)
    object LCSongs : ProfileScreens("LCSongs", R.string.scrolling_songs_screen)
}