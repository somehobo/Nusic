package com.njbrady.nusic

import android.widget.Button
import android.widget.ImageButton
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow.Companion.Clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import com.njbrady.nusic.profile.utils.ProfilePhoto
import com.njbrady.nusic.ui.theme.NusicTheme

@Composable
fun ProfileScreen(mainViewModel: MainViewModel) {
    ProfileScreenContent(mainViewModel = mainViewModel)
}

@Composable
private fun ProfileScreenContent(
    mainViewModel: MainViewModel
) {

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

            item {
                MusicSelectionTab(modifier = Modifier.fillMaxWidth(), currentlySelected = currentlySelected, onFilter = {newFilter -> if (newFilter != currentlySelected) currentlySelected = newFilter})
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
    TabRow(selectedTabIndex = currentlySelected.ordinal,
        indicator = { tabPositions: List<TabPosition> ->
            Box(
                Modifier
                    .tabIndicatorOffset(tabPositions[currentlySelected.ordinal])
                    .fillMaxSize()
                    .padding(horizontal = 4.dp)
                    .border(BorderStroke(2.dp, Color.White), RoundedCornerShape(16.dp))
            )
        },
        divider = { }
    ) {
        SongFilterTabs.values().forEachIndexed() { index, songFilterTab ->
            val selected = index == currentlySelected.ordinal

            val textModifier = Modifier
                .padding(vertical = 8.dp, horizontal = 16.dp)

            Tab(
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .clip(RoundedCornerShape(16.dp)),
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