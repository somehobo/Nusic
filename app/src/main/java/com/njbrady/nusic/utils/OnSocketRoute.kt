package com.njbrady.nusic.utils

import org.json.JSONObject

data class OnSocketRoute(
    val route: String,
    val callback: (JSONObject) -> Unit
) {
    companion object {
        const val PROFILEROUTE = "profile"
        const val HOMEROUTE = "home"
        const val DEFAULTROUTE = "default"
    }
}


