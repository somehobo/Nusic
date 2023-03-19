package com.njbrady.nusic.upload

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.njbrady.nusic.utils.composables.NavigationTopAppBar

@Composable
fun UploadScreen(
    uploadScreenViewModel: UploadScreenViewModel,
    navController: NavController
) {
    Scaffold(topBar = {
        NavigationTopAppBar(
            navController = navController,
            title = "Upload"
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
    Column(modifier = Modifier.fillMaxSize().padding(paddingValues = paddingValues)) {

    }
}
