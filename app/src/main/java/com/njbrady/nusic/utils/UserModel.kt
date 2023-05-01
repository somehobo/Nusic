package com.njbrady.nusic.utils

import com.njbrady.nusic.utils.GeneralKeys.ID
import com.njbrady.nusic.utils.GeneralKeys.USERNAME
import org.json.JSONObject

data class UserModel(
    val userName: String,
    val id: Int
) {
    companion object {
        fun fromJson(jsonObject: JSONObject): UserModel{
            return UserModel(jsonObject.getString(USERNAME), jsonObject.getInt(ID))
        }
    }
}