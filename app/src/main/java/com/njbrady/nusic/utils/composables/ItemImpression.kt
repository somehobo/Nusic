package com.njbrady.nusic.utils.composables

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.*


// credit: [https://plusmobileapps.com/2022/05/04/lazy-column-view-impressions.html]
@Composable
fun ItemImpression(index: Int, lazyListState: LazyListState, onItemViewed: () -> Unit) {
    val isItemWithKeyInView by remember {
        derivedStateOf {
            val halfViewPort = lazyListState.layoutInfo.viewportSize.height / 2

            lazyListState.layoutInfo
                .visibleItemsInfo
                .any { it.index == index && it.offset > -halfViewPort && it.offset < halfViewPort }
        }
    }
    if (isItemWithKeyInView) {
        LaunchedEffect(Unit) { onItemViewed() }
    }
}
