package com.njbrady.nusic


import androidx.annotation.StringRes
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemsIndexed
import coil.compose.SubcomposeAsyncImage
import com.njbrady.nusic.home.responseObjects.SongObject
import com.njbrady.nusic.profile.composables.ProfileScrollingSongs
import com.njbrady.nusic.profile.requests.Type
import com.njbrady.nusic.profile.utils.ProfilePhoto
import com.njbrady.nusic.ui.theme.NusicTheme


@Composable
fun ProfileScreen(mainViewModel: MainViewModel) {
    ProfileScrenNavigation(mainViewModel = mainViewModel)
}

@Composable
private fun ProfileScrenNavigation(mainViewModel: MainViewModel) {
    val navController = rememberNavController()
    var currentlySelected by remember {
        mutableStateOf(Type.Liked)
    }
    var selectedSong = 0

    Scaffold { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = ProfileScreens.Profile.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(ProfileScreens.Profile.route) {
                ProfileScreenContent(mainViewModel = mainViewModel,
                    currentlySelected = currentlySelected,
                    onFilter = { newFilter -> currentlySelected = newFilter },
                    onSelected = { songObject ->
                        selectedSong = songObject
                        navController.navigate(ProfileScreens.LCSongs.route)
                    })
            }
            composable(ProfileScreens.LCSongs.route) {
                ProfileScrollingSongs(
                    mainViewModel = mainViewModel,
                    navController = navController,
                    selectedSongIndex = selectedSong,
                    type = currentlySelected
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalAnimationApi::class)
@Composable
private fun ProfileScreenContent(
    mainViewModel: MainViewModel,
    currentlySelected: Type,
    onFilter: (Type) -> Unit,
    onSelected: (Int) -> Unit
) {

    val likedSongs = mainViewModel.likedSongs.collectAsLazyPagingItems()
    val createdSongs = mainViewModel.createdSongs.collectAsLazyPagingItems()
    val displayedSongs = if (currentlySelected == Type.Liked) likedSongs else createdSongs

    Scaffold(topBar = { ProfileScreenHeader(mainViewModel) }) { paddingValues ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = dimensionResource(id = R.dimen.NusicDimenX4)),
                    horizontalArrangement = Arrangement.Center
                ) {
                    ProfilePhotoComposable(ProfilePhoto())
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

            itemsIndexed(items = displayedSongs) { index, item ->
                item?.songObject?.let {
                    MusicElement(songObject = it, onSelected = onSelected, index = index)
                    Divider()
                }

            }
        }
    }
}

@Composable
private fun MusicElement(songObject: SongObject, onSelected: (Int) -> Unit, index: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelected(index) },
        verticalAlignment = Alignment.CenterVertically
    ) {
        SongPicture(
            modifier = Modifier
                .padding(dimensionResource(id = R.dimen.NusicDimenX1))
                .size(dimensionResource(id = R.dimen.NusicDimenX5))
                .clip(
                    RoundedCornerShape(dimensionResource(id = R.dimen.NusicDimenX1))
                )
                .background(MaterialTheme.colors.primary)
                .padding(dimensionResource(id = R.dimen.NusicDimenX1)), songObject = songObject
        )

        SongNameWithArtist(
            name = songObject.name,
            artist = songObject.artist
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
private fun SongPicture(modifier: Modifier = Modifier, songObject: SongObject) {
    Box(
        modifier = modifier
    ) {
        if (songObject.imageUrl != null) {
            SubcomposeAsyncImage(
                modifier = Modifier.fillMaxSize(),
                model = songObject.imageUrl,
                loading = {
                    Box(
                        modifier = Modifier.background(colorResource(id = R.color.card_overlay)),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(
                                dimensionResource(
                                    id = R.dimen.NusicDimenX8
                                )
                            )
                        )
                    }
                },
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

            val textModifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp)

            Tab(modifier = Modifier, selected = selected, onClick = {
                onFilter(type)
            }) {
                Text(
                    modifier = textModifier, text = type.name
                )
            }
        }
    }

}

@Composable
private fun ProfilePhotoComposable(profilePhoto: ProfilePhoto) {
    val profilePhotoState by profilePhoto.profilePhotoState.collectAsState()
    val photoUrl by profilePhoto.photoUrl.collectAsState()

    Box(modifier = Modifier
        .clip(shape = CircleShape)
        .clickable { }) {
        photoUrl?.let {
            SubcomposeAsyncImage(
                modifier = Modifier.size(dimensionResource(id = R.dimen.ProfileImageDimen)),
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
            )
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