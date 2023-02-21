package com.njbrady.nusic.utils

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Color

fun Modifier.glowLoad(): Modifier = composed {
    val transition = rememberInfiniteTransition()
    val translateAnimation by transition.animateColor(
        initialValue = Color.Black.copy(alpha = 0.3f),
        targetValue = Color.Black.copy(alpha = 0.6f),
        animationSpec = infiniteRepeatable(animation = tween(durationMillis = 1000), repeatMode = RepeatMode.Reverse )
    )
    return@composed this.then(background(translateAnimation))
}