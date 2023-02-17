package com.njbrady.nusic.utils.composables

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue

// credit: [https://plusmobileapps.com/2022/05/04/lazy-column-view-impressions.html]
@Composable
fun ItemImpression(index: Int, lazyListState: LazyListState, onItemViewed: () -> Unit) {
    val isItemWithKeyInView by remember {
        derivedStateOf {
            lazyListState.layoutInfo
                .visibleItemsInfo
                .any { it.index == index && it.offset == 0 }
        }
    }
    if (isItemWithKeyInView) {
        LaunchedEffect(Unit) { onItemViewed() }
    }
}
