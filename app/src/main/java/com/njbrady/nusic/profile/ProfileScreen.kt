package com.njbrady.nusic


import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.items
import coil.compose.SubcomposeAsyncImage
import com.njbrady.nusic.home.SongCard
import com.njbrady.nusic.home.utils.SongCardStateStates
import com.njbrady.nusic.profile.utils.ProfilePhoto
import com.njbrady.nusic.ui.theme.NusicTheme

@Composable
fun ProfileScreen(mainViewModel: MainViewModel) {
    ProfileScreenContent(mainViewModel = mainViewModel)
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ProfileScreenContent(
    mainViewModel: MainViewModel
) {

    val likedSongs = mainViewModel.likedSongs.collectAsLazyPagingItems()

    var currentlySelected by remember {
        mutableStateOf(SongFilterTabs.Created)
    }

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
                MusicSelectionTab(modifier = Modifier.fillMaxWidth(), currentlySelected = currentlySelected, onFilter = {newFilter -> if (newFilter != currentlySelected) currentlySelected = newFilter})
            }

            items(likedSongs) { item ->
                item?.let {
                    SongCard(modifier = Modifier.fillMaxWidth(0.8f).fillMaxHeight(0.5f),
                        songCardStateState = SongCardStateStates.Playing,
                        songObject = item,
                        errorMessage = ""
                    )
                }
            }
        }
    }
}

@Composable
private fun MusicSelectionTab(
    modifier: Modifier = Modifier,
    currentlySelected: SongFilterTabs,
    onFilter: (SongFilterTabs) -> Unit
) {
    TabRow(
        modifier = modifier,
        selectedTabIndex = currentlySelected.ordinal,
        backgroundColor = MaterialTheme.colors.background,
        indicator = { tabPositions: List<TabPosition> ->
            Box(
                Modifier
                    .tabIndicatorOffset(tabPositions[currentlySelected.ordinal])
                    .fillMaxSize()
            ) {
                Divider(modifier = Modifier.align(Alignment.BottomCenter),color = MaterialTheme.colors.onBackground)
            }
        },
        divider = { }
    ) {
        SongFilterTabs.values().forEachIndexed() { index, songFilterTab ->
            val selected = index == currentlySelected.ordinal

            val textModifier = Modifier
                .padding(vertical = 8.dp, horizontal = 16.dp)

            Tab(
                modifier = Modifier,
                selected = selected,
                onClick = {
                    onFilter(songFilterTab)
                }
            ) {
                Text(
                    modifier = textModifier,
                    text = songFilterTab.name
                )
            }
        }
    }

}

@Composable
private fun ProfilePhotoComposable(profilePhoto: ProfilePhoto) {
    val profilePhotoState by profilePhoto.profilePhotoState.collectAsState()
    val photoUrl by profilePhoto.photoUrl.collectAsState()

    Box(
        modifier = Modifier
            .clip(shape = CircleShape)
            .clickable { }
    ) {
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
                contentDescription = "Profile Image",
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
                    contentDescription = "Upload Button"
                )
            }
            Box(
                modifier = Modifier
                    .wrapContentSize(Alignment.TopStart)
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
                        contentDescription = "Settings Button"
                    )
                }
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    DropdownMenuItem(onClick = { mainViewModel.logout() }) {
                        Text("Logout", color = MaterialTheme.colors.error)
                    }
                }
            }
        })
}


//@Composable
//private fun ProfileScreenDropDownMenu(expanded: Boolean, onLogout: () -> Unit) {
//    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
//        DropdownMenuItem(onClick = { onLogout() }) {
//            Text("Settings")
//        }
//    }
//}

@Composable
@Preview(showBackground = true)
private fun viewer() {
    NusicTheme {
        ProfilePhotoComposable(ProfilePhoto())
    }
}

enum class SongFilterTabs {
    Liked, Created
}