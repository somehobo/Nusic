package com.njbrady.nusic.profile.utils

import com.njbrady.nusic.login.requests.LoginJsonKeys
import com.njbrady.nusic.utils.GeneralKeys.BIO
import com.njbrady.nusic.utils.toList
import org.json.JSONObject

data class UploadBioErrorModel(
    val bioErrors: List<String>? = null,
    val generalError: String? = null
) {
    companion object {
        fun fromJson(jsonObject: JSONObject): UploadBioErrorModel {
            return UploadBioErrorModel(
                bioErrors = jsonObject.getJSONArray(BIO).toList() as List<String>,
                generalError = jsonObject.getString(LoginJsonKeys.ErrorKey)
            )
        }
    }
}