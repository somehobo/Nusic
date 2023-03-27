package com.njbrady.nusic.upload

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Done
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.njbrady.nusic.R
import com.njbrady.nusic.ui.theme.NusicBlue
import com.njbrady.nusic.ui.theme.NusicSeeThroughBlack
import com.njbrady.nusic.ui.theme.NusicTheme
import com.njbrady.nusic.utils.composables.EditableSongCard
import com.njbrady.nusic.utils.composables.NavigationTopAppBar

@Composable
fun UploadScreen(
    uploadScreenViewModel: UploadScreenViewModel,
    navController: NavController
) {
    Scaffold(topBar = {
        NavigationTopAppBar(
            navController = navController,
            title = "Upload",
            actions = {
                IconButton(
                    onClick = { uploadScreenViewModel.clearState() },
                    modifier = Modifier
                        .size(dimensionResource(id = R.dimen.NusicDimenX5))
                        .padding(
                            end = dimensionResource(id = R.dimen.NusicDimenX1)
                        )
                ) {
                    Icon(
                        modifier = Modifier.size(dimensionResource(id = R.dimen.NusicDimenX5)),
                        imageVector = Icons.Outlined.Close, contentDescription = "Cancel Button"
                    )

                }
                IconButton(
                    onClick = { /* on upload here */ },
                    modifier = Modifier
                        .size(dimensionResource(id = R.dimen.NusicDimenX5))
                        .padding(
                            end = dimensionResource(id = R.dimen.NusicDimenX1)
                        )
                ) {
                    Icon(
                        modifier = Modifier.size(dimensionResource(id = R.dimen.NusicDimenX5)),
                        imageVector = Icons.Outlined.Done,
                        tint = NusicBlue,
                        contentDescription = "Upload Button"
                    )
                }
            }
        )
    }) { paddingValues ->
        UploadScreenContent(
            uploadScreenViewModel = uploadScreenViewModel,
            onUpload = { navController.navigateUp() },
            paddingValues = paddingValues
        )
    }
}

@Composable
private fun UploadScreenContent(
    uploadScreenViewModel: UploadScreenViewModel,
    onUpload: () -> Unit,
    paddingValues: PaddingValues
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues = paddingValues)
    ) {
        EditableSongCard(
            viewModel = uploadScreenViewModel,
            modifier = Modifier
                .fillMaxSize()
                .padding(dimensionResource(id = R.dimen.NusicDimenX1))
                .weight(1f)
        )
        //FFT Upload thing
        UploadSong(
            modifier = Modifier
                .fillMaxWidth()
                .height(dimensionResource(id = R.dimen.NusicDimenX10)),
            uploadScreenViewModel = uploadScreenViewModel,
            onUpload = onUpload
        )
    }
}

@Composable
private fun UploadSong(
    modifier: Modifier = Modifier,
    uploadScreenViewModel: UploadScreenViewModel,
    onUpload: () -> Unit
) {
    val songUrl by uploadScreenViewModel.songUrl.collectAsState()

    Column(modifier = modifier) {
        if (songUrl == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        start = dimensionResource(
                            id = R.dimen.NusicDimenX1
                        ), end = dimensionResource(
                            id = R.dimen.NusicDimenX1
                        ), bottom = dimensionResource(
                            id = R.dimen.NusicDimenX1
                        )
                    )
                    .clip(RoundedCornerShape(dimensionResource(id = R.dimen.NusicDimenX1)))
                    .background(MaterialTheme.colors.primary)
                    .clickable {  },
                contentAlignment = Alignment.Center
            ) {
                backgroundPowerSpectrum(modifier = Modifier.padding(vertical = dimensionResource(id = R.dimen.NusicDimenX1)))
                Icon(
                    modifier = Modifier.size(dimensionResource(id = R.dimen.NusicDimenX7)),
                    imageVector = Icons.Filled.AddCircle, contentDescription = "Add Image"
                )
            }
        } else {
            // FFT Editor
        }
    }
}

@Composable
private fun backgroundPowerSpectrum(modifier: Modifier = Modifier) {
    val content = listOf(100, 40, 100, 30, 10, 60, 20, 60, 100, 5, 10, 70, 100, 40, 100, 30, 10, 60, 20, 60, 100, 5, 10, 70, 100, 40, 100, 30, 10, 60, 20, 60, 100, 5, 10, 70, 100, 40, 100, 30, 10, 60, 20, 60, 100, 5, 10, 70, 100, 40, 100, 30, 10, 60, 20, 60, 100, 5, 10, 70, 100, 40, 100, 30, 10, 60, 20, 60, 100, 5, 10, 70, 100, 40, 100, 30, 10, 60, 20, 60, 100, 5, 10, 70, 100, 40, 100, 30, 10, 60, 20, 60, 100, 5, 10, 70, 100)
    Row(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        content.forEach {
            Box(
                modifier = Modifier
                    .width(dimensionResource(id = R.dimen.NusicDimenXHalf))
                    .fillMaxHeight((it.toFloat() / 100f))
                    .background(MaterialTheme.colors.primaryVariant)
                    .border(border = BorderStroke(width = 0.5.dp, color = NusicSeeThroughBlack))
            )
        }
    }
}

//@Composable
//private fun PowerSpectrumEditor(
//    modifier: Modifier = Modifier,
//    uploadScreenViewModel: UploadScreenViewModel,
//
//    ) {
//    val content = listOf(100, 40, 100, 30, 10, 60, 20, 60, 100, 5, 10, 70, 100)
//    LazyRow(modifier =) {
//        items(content) { height ->
//            Box(
//                modifier = Modifier
//                    .width(dimensionResource(id = R.dimen.NusicDimenX1))
//                    .height(
//                        h
//                    )
//            )
//        }
//    }
//}


@Composable
@Preview(showBackground = true)
private fun defaultPreview() {
    NusicTheme {

        backgroundPowerSpectrum()
    }
}
