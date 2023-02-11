package com.njbrady.nusic.profile.utils

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.njbrady.nusic.home.responseObjects.SongObject
import com.njbrady.nusic.profile.requests.Type
import com.njbrady.nusic.profile.requests.pagedRequest
import com.njbrady.nusic.utils.TokenStorage

class ProfileGridDataSource(private val tokenStorage: TokenStorage, private val type: Type) : PagingSource<Int, SongObject>() {
    override fun getRefreshKey(state: PagingState<Int, SongObject>): Int? {
        return state.anchorPosition
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, SongObject> {
        return try {
            val pageToGet = params.key ?: 1
            val pagedResponse = pagedRequest(tokenStorage, pageToGet, type)
            return LoadResult.Page(
                data = pagedResponse.songObjects,
                prevKey = if (pageToGet == 1) null else pageToGet - 1,
                nextKey = if (pagedResponse.songObjects.isEmpty()) null else pagedResponse.page + 1
            )
        } catch (exception: Exception) {
            return LoadResult.Error(exception)
        }
    }
}
