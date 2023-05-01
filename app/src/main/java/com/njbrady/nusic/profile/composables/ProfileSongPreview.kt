package com.njbrady.nusic.profile.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import coil.compose.SubcomposeAsyncImage
import com.njbrady.nusic.R
import com.njbrady.nusic.home.model.SongModel
import com.njbrady.nusic.utils.shimmerBackground


@Composable
fun MusicElement(songModel: SongModel, onSelected: (Int) -> Unit, index: Int) {
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
                ), songObject = songModel
        )

        SongNameWithArtist(
            name = songModel.name, artist = songModel.userModel.userName
        )
    }
}

@Composable
private fun SongPreviewPicture(modifier: Modifier = Modifier, songObject: SongModel) {
    Box(
        modifier = modifier
    ) {
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

@Composable
private fun SongNameWithArtist(name: String, artist: String) {
    Column {
            Text(
                modifier = Modifier.padding(
                    start = dimensionResource(id = R.dimen.NusicDimenX1),
                    top = dimensionResource(id = R.dimen.NusicDimenX1)
                ), text = name
            )


            Text(
                modifier = Modifier.padding(
                    start = dimensionResource(id = R.dimen.NusicDimenX1),
                    bottom = dimensionResource(id = R.dimen.NusicDimenX1)
                ), text = "- $artist", style = MaterialTheme.typography.caption
            )

    }
}
