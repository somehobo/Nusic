package com.njbrady.nusic.home.utils

import com.njbrady.nusic.home.requests.getSong
import com.njbrady.nusic.home.requests.likeSong
import com.njbrady.nusic.home.model.SongModel
import com.njbrady.nusic.home.model.SongObjectErrorWrapper
import com.njbrady.nusic.utils.TokenStorage


abstract class MusicJob(val tokenStorage: TokenStorage) {

    // If network error or other this should throw an error
    abstract fun runJob(): SongObjectErrorWrapper
}


class GetSongJob(tokenStorage: TokenStorage) : MusicJob(tokenStorage) {
    override fun runJob(): SongObjectErrorWrapper {
        return getSong(tokenStorage)
    }
}

class LikeSongJob(
    private val songObject: SongModel,
    private val liked: Boolean,
    tokenStorage: TokenStorage
) : MusicJob(tokenStorage) {
    override fun runJob(): SongObjectErrorWrapper {
        return likeSong(songObject, liked, tokenStorage)
    }
}
