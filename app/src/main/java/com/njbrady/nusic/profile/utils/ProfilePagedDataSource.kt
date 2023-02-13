package com.njbrady.nusic.profile.utils

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.njbrady.nusic.home.responseObjects.SongObject
import com.njbrady.nusic.home.utils.SongCardState
import com.njbrady.nusic.profile.requests.Type
import com.njbrady.nusic.profile.requests.pagedRequest
import com.njbrady.nusic.utils.TokenStorage

class ProfilePagedDataSource(private val tokenStorage: TokenStorage, private val type: Type) : PagingSource<Int, SongCardState>() {
    override fun getRefreshKey(state: PagingState<Int, SongCardState>): Int? {
        return state.anchorPosition
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, SongCardState> {
        return try {
            val pageToGet = params.key ?: 1
            val pagedResponse = pagedRequest(tokenStorage, pageToGet, type)
            val songStates = mutableListOf<SongCardState>()
            pagedResponse.songObjects.forEach {
                songStates.add(SongCardState(it))
            }
            return LoadResult.Page(
                data = songStates,
                prevKey = if (pageToGet == 1) null else pageToGet - 1,
                nextKey = if (pagedResponse.songObjects.isEmpty()) null else pagedResponse.page + 1
            )
        } catch (exception: Exception) {
            return LoadResult.Error(exception)
        }
    }
}