package com.njbrady.nusic.home.responseObjects

data class SongObjectErrorWrapper(
    val nonBlockingError: String? = null,
    val blockingError: String? = null,
    val songObject: SongObject? = null
)