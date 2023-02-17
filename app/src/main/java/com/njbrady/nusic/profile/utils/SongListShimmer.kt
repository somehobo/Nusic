package com.njbrady.nusic.profile.utils

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import com.njbrady.nusic.MainViewModel
import com.njbrady.nusic.login.composables.CenteredProgressIndicator
import com.njbrady.nusic.utils.shimmerBackground
import com.njbrady.nusic.R

@Composable
fun SongListInitialCommunicatedState(loadState: CombinedLoadStates) {
    when (val state = loadState.refresh) {
        is LoadState.Error -> {
            Toast.makeText(
                LocalContext.current, state.error.message, Toast.LENGTH_SHORT
            ).show()
        }
        is LoadState.Loading -> {
            CenteredProgressIndicator()
        }
        else -> {}
    }
}

@Composable
fun SongListFurtherCommunicatedState(loadState: CombinedLoadStates) {
    when (val state = loadState.append) { // Pagination
        is LoadState.Error -> {
            if (!loadState.prepend.endOfPaginationReached) {
                Toast.makeText(
                    LocalContext.current, state.error.message, Toast.LENGTH_SHORT
                ).show()
            }
        }
        is LoadState.Loading -> { // Pagination Loading UI
            repeat(MainViewModel.PAGE_SIZE) {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Box(
                        modifier = Modifier
                            .padding(dimensionResource(id = R.dimen.NusicDimenX1))
                            .size(dimensionResource(id = R.dimen.NusicDimenX5))
                            .shimmerBackground(RoundedCornerShape(dimensionResource(id = R.dimen.NusicDimenX1)))
                            .padding(dimensionResource(id = R.dimen.NusicDimenX1))
                    )

                    Column {
                        Box(
                            modifier = Modifier
                                .size(
                                    height = dimensionResource(id = R.dimen.NusicDimenX3),
                                    width = dimensionResource(
                                        id = R.dimen.ShimmerSongNameDimen
                                    )
                                )
                                .padding(
                                    start = dimensionResource(id = R.dimen.NusicDimenX1),
                                    top = dimensionResource(id = R.dimen.NusicDimenX1)
                                )
                                .shimmerBackground(
                                    RectangleShape
                                )
                        )

                        Box(
                            modifier = Modifier
                                .padding(
                                    top = dimensionResource(id = R.dimen.NusicDimenX1),
                                    start = dimensionResource(id = R.dimen.NusicDimenX1),
                                    bottom = dimensionResource(id = R.dimen.NusicDimenX1)
                                )
                                .size(
                                    height = dimensionResource(id = R.dimen.NusicDimenX1),
                                    width = dimensionResource(
                                        id = R.dimen.NusicDimenX3
                                    )
                                )
                                .shimmerBackground(
                                    RectangleShape
                                )
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .height(dimensionResource(id = R.dimen.BorderStrokeSize))
                        .fillMaxWidth()
                        .shimmerBackground(RectangleShape)
                )
            }
        }
        else -> {}
    }
}