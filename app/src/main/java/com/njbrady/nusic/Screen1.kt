package com.njbrady.nusic

import android.view.Window
import android.view.WindowInsets
import android.view.WindowInsets.Type.navigationBars
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.core.view.WindowInsetsCompat.Type.navigationBars

@Composable
fun MainScreen1() {
    Scaffold {
        Text("screen 1")
    }
}
