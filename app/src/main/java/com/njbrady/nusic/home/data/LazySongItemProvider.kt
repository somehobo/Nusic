package com.njbrady.nusic.home.data

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.lazy.layout.LazyLayoutItemProvider

@OptIn(ExperimentalFoundationApi::class)
class LazySongItemProvider(val items: MutableList<Song>, onLikeAction: (Song, Boolean) -> Unit) : LazyLayoutItemProvider {
    override val itemCount: Int
        get() = items.size

    override fun Item(index: Int) {
        return
    }

}