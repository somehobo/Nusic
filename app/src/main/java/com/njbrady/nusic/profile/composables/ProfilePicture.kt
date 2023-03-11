package com.njbrady.nusic.profile.composables

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import coil.compose.SubcomposeAsyncImage
import com.njbrady.nusic.R
import com.njbrady.nusic.profile.utils.ProfilePhoto
import com.njbrady.nusic.profile.utils.ProfilePhotoState
import com.njbrady.nusic.utils.glowLoad

@Composable
fun ProfilePhotoComposable(profilePhoto: ProfilePhoto) {
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

    val loadState: @Composable () -> Unit = {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .glowLoad()
        )
    }
    Box(modifier = Modifier
        .clip(shape = CircleShape)
        .clickable {
            if (profilePhotoState != ProfilePhotoState.SuccessPending) selectImageLauncher.launch(
                "image/*"
            )
        }
        .size(dimensionResource(id = R.dimen.ProfileImageDimen))) {
        photoUrl?.let {
            SubcomposeAsyncImage(
                modifier = Modifier.fillMaxSize(),
                model = it,
                loading = {
                    loadState()
                },
                contentDescription = stringResource(id = R.string.profile_image),
                contentScale = ContentScale.Crop
            )
            if (profilePhotoState == ProfilePhotoState.SuccessPending) {
                loadState()
            }
        }
    }
}
