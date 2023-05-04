package com.njbrady.nusic.profile.utils

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.njbrady.nusic.profile.requests.SongListType
import com.njbrady.nusic.profile.requests.pagedRequest
import com.njbrady.nusic.utils.ExoMiddleMan
import com.njbrady.nusic.utils.LocalStorage
import com.njbrady.nusic.utils.SongPlayerWrapper
import com.njbrady.nusic.utils.UserModel

class ProfilePagedDataSource(private val localStorage: LocalStorage, private val songListType: SongListType, private val exoMiddleMan: ExoMiddleMan, private val userModel: UserModel? = null) : PagingSource<Int, SongPlayerWrapper>() {
    override fun getRefreshKey(state: PagingState<Int, SongPlayerWrapper>): Int {
        return 1
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, SongPlayerWrapper> {
        return try {
            val pageToGet = params.key ?: 1
            val pagedResponse = pagedRequest(localStorage, pageToGet, songListType, userModel)
            val songPlayerWrappers = mutableListOf<SongPlayerWrapper>()
            pagedResponse.songModels.forEach {
                songPlayerWrappers.add(exoMiddleMan.addMedia(it))
            }
            return LoadResult.Page(
                data = songPlayerWrappers,
                prevKey = if (pageToGet == 1) null else pageToGet - 1,
                nextKey = if (pagedResponse.songModels.isEmpty()) null else pagedResponse.page + 1
            )
        } catch (exception: Exception) {
            return LoadResult.Error(exception)
        }
    }


}
