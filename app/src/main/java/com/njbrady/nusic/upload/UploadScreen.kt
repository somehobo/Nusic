package com.njbrady.nusic.upload

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Done
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.core.view.WindowCompat
import androidx.navigation.NavController
import com.njbrady.nusic.utils.composables.EditableSongCard
import com.njbrady.nusic.utils.composables.NavigationTopAppBar
import com.njbrady.nusic.R
import com.njbrady.nusic.ui.theme.NusicBlue

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
        )
        //FFT Upload thing
        UploadSong(
            modifier = Modifier
                .fillMaxWidth()
                .height(dimensionResource(id = R.dimen.NusicDimenX6)),
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
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                //PLUS BUTTON
            }
        } else {
            // FFT Editor
        }
    }
}

@Composable
private fun PowerSpectrumEditor(
    modifier: Modifier = Modifier,
    uploadScreenViewModel: UploadScreenViewModel,

    ) {

}
