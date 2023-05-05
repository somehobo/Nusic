package com.njbrady.nusic.home.utils

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.tween
import androidx.compose.runtime.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp

/*
enumerates possible swiping directions
 */
enum class Direction {
    Left, Right
}


@Composable
fun rememberSwipeableCardState(): SwipeableCardState {
    val swipeableCardState = getSwipeableCardState()

    return remember {
        swipeableCardState
    }
}


@Composable
fun getSwipeableCardState(): SwipeableCardState {
    val screenWidth = with(LocalDensity.current) {
        LocalConfiguration.current.screenWidthDp.dp.toPx()
    }
    val screenHeight = with(LocalDensity.current) {
        LocalConfiguration.current.screenHeightDp.dp.toPx()
    }
    return SwipeableCardState(screenWidth, screenHeight)
}


class SwipeableCardState(
    internal val maxWidth: Float,
    internal val maxHeight: Float,
) {
    var offset = Animatable(offset(0f, 0f), Offset.VectorConverter)

    /**
     * The [Direction] the card was swiped at.
     *
     * Null value means the card has not been swiped fully yet.
     */
    var swipedDirection: Direction? by mutableStateOf(null)

    internal suspend fun reset() {
        offset.animateTo(offset(0f, 0f), tween(400))
    }

    fun resetInstant() {
        offset = Animatable(offset(0f, 0f), Offset.VectorConverter)
        swipedDirection = null
    }

    /*
    animates the finished swipe with 800 ms duration
     */
    suspend fun finishSwipe(
        direction: Direction,
        animationSpec: AnimationSpec<Offset> = tween(400)
    ) {
        val endX =
            maxWidth * 1.5f //I believe multiplier is due to cards rotation, this is used when auto completing the swipe
//        val endY = maxHeight
        when (direction) {
            Direction.Left -> offset.animateTo(offset(x = -endX), animationSpec)
            Direction.Right -> offset.animateTo(offset(x = endX), animationSpec)
//            Direction.Up -> offset.animateTo(offset(y = -endY), animationSpec)
//            Direction.Down -> offset.animateTo(offset(y = endY), animationSpec)
        }
        this.swipedDirection = direction
    }

    private fun offset(x: Float = offset.value.x, y: Float = offset.value.y): Offset {
        return Offset(x, y)
    }

    /*
    animates current drag!
     */
    internal suspend fun performDrag(x: Float, y: Float) {
        offset.animateTo(offset(x, y))
    }
}