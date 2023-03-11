package com.njbrady.nusic.utils

import com.njbrady.nusic.MainSocketHandler
import org.json.JSONObject

abstract class MessageHandler(
    private val route: String,
    private val mainSocketHandler: MainSocketHandler,
) {

    init {
        mainSocketHandler.subscribeNewRoute(
            route = OnSocketRoute.HOMEROUTE
        ) { jsonObject -> onMessageRecieved(jsonObject = jsonObject) }
    }

    fun onClear() {
        mainSocketHandler.unsubscribeRoute(route)
    }

    fun sendMessage(jsonObject: JSONObject) {
        mainSocketHandler.send(jsonObject)
    }

    abstract fun onMessageRecieved(jsonObject: JSONObject)

}