package com.njbrady.nusic.profile.composables

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.njbrady.nusic.R
import com.njbrady.nusic.profile.requests.SongListType


@Composable
fun ProfileUsername(modifier: Modifier = Modifier, username: String) {
    val adaptiveTextName = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        letterSpacing = 0.25.sp,
        background = MaterialTheme.colors.onBackground
    )
    Text(
        modifier = Modifier,
        text = username,
        style = adaptiveTextName,
        color = MaterialTheme.colors.background
    )
}


@Composable
fun MusicSelectionTab(
    modifier: Modifier = Modifier, currentlySelected: SongListType, onFilter: (SongListType) -> Unit
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
                Divider(
                    modifier = Modifier.align(Alignment.BottomCenter),
                    color = MaterialTheme.colors.onBackground
                )
            }
        },
    ) {
        SongListType.values().forEachIndexed() { index, type ->
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

