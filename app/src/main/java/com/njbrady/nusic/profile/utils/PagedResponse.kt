package com.njbrady.nusic.profile.utils

import com.njbrady.nusic.home.model.SongObject
import org.json.JSONObject

data class PagedResponse(
    var songObjects: List<SongObject>,
    var page: Int,
    var per_page: Int,
    var total: Int,
) {
    companion object {
        fun fromJson(jsonObject: JSONObject): PagedResponse {
            return try {
                val total = jsonObject.getInt(ProfileKeys.totalKey)
                val songList = mutableListOf<SongObject>()
                val jsonArray = jsonObject.optJSONArray(ProfileKeys.songObjectsKey)
                if (jsonArray != null && jsonArray.length() != 0) {
                    for (index in 0 until total) {
                        songList.add(SongObject.fromJson(jsonArray.getJSONObject(index)))
                    }
                }
                PagedResponse(
                    songObjects = songList,
                    page = jsonObject.getInt(ProfileKeys.pageKey),
                    per_page = jsonObject.getInt(ProfileKeys.perPageKey),
                    total = total
                )
            } catch (exception: Exception) {
                throw Exception("Key missmatch")
            }
        }
    }
}