package com.njbrady.nusic.utils

import com.njbrady.nusic.home.model.SongModel
import java.util.*

data class SongPlayerWrapper(
    val songModel: SongModel,
    val uuid: UUID,
    val play: () -> Unit,
    val remove: () -> Unit,
    val restart: () -> Unit,
    val reset: () -> Unit
)