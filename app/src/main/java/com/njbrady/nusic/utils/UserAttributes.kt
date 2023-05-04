package com.njbrady.nusic.utils

import com.njbrady.nusic.utils.GeneralKeys.BIO
import com.njbrady.nusic.utils.GeneralKeys.PROFILEPHOTO
import com.njbrady.nusic.utils.GeneralKeys.USERKEY
import org.json.JSONObject

data class UserAttributes(
    val profilePhotoUrl: String,
    val bio: String?,
    val userModel: UserModel
) {
    companion object {
        fun fromJson(jsonObject: JSONObject): UserAttributes {
            val bio = jsonObject.getString(BIO)

            return UserAttributes(
                profilePhotoUrl = jsonObject.getString(PROFILEPHOTO),
                bio = if (bio == "null") null else bio,
                userModel = UserModel.fromJson(jsonObject.getJSONObject(USERKEY))
            )
        }
    }
}