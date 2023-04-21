package com.njbrady.nusic.upload.model

data class UploadSongRepository(
    val titleErrors: List<String>? = null,
    val songErrors: List<String>? = null,
    val songPhotoErrors: List<String>? = null,
    val timeErrors: List<String>? = null,
    val generalErrors: List<String>? = null,
    val containsError: Boolean = false
)