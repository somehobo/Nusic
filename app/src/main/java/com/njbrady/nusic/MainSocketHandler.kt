package com.njbrady.nusic

import android.util.Log
import com.njbrady.nusic.utils.HttpOptions
import com.njbrady.nusic.utils.OnSocketRoute
import com.njbrady.nusic.utils.TokenStorage
import com.njbrady.nusic.utils.UrlProvider.Companion.baseWebSocketUrl
import kotlinx.coroutines.flow.MutableStateFlow
import okhttp3.*
import okio.ByteString
import org.json.JSONObject
import java.util.*


class MainSocketHandler(private val okHttpClient: OkHttpClient, private val tokenStorage: TokenStorage) : WebSocketListener() {

    private val _messageQueue = LinkedList<String>()
    private val _socketRoutes = mutableListOf<OnSocketRoute>()
    private var _connected = MutableStateFlow(false)

    private lateinit var _webSocket: WebSocket

    init {
        Log.e("MAINSOCKETHANDLER", "socket handler created")
        openConnection()
    }

    private fun openConnection() {
        val request = Request.Builder()
            .url(baseWebSocketUrl)
            .header(HttpOptions.Authorization, tokenStorage.retrieveToken())
            .build()

        _webSocket = okHttpClient.newWebSocket(request, this)
    }

    fun subscribeNewRoute(route: String, callback: (JSONObject) -> Unit) {
        unsubscribeRoute(route = route)
        _socketRoutes.add(OnSocketRoute(route = route, callback = callback))
    }

    fun unsubscribeRoute(route: String) {
        _socketRoutes.removeIf { onSocketRoute ->
            onSocketRoute.route == route
        }
    }

    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
        super.onClosed(webSocket, code, reason)
        _connected.value = false
    }

    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
        super.onClosing(webSocket, code, reason)
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        super.onFailure(webSocket, t, response)
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        super.onMessage(webSocket, text)
        val message = JSONObject(text)
        Log.e("MAINSOCKETHANDLER", message.toString())
        val route = message.optString(ROUTEKEY, OnSocketRoute.DEFAULTROUTE)

        val relevantSubscribers = _socketRoutes.filter { onSocketRoute ->
            onSocketRoute.route == route
        }
        if (relevantSubscribers.isEmpty()) {
            Log.e("MAINSOCKETHANDLER", "no subscribed routes")
        }
        relevantSubscribers.forEach { onSocketRoute ->
            onSocketRoute.callback(message)
        }
    }

    override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
        super.onMessage(webSocket, bytes)
    }

    override fun onOpen(webSocket: WebSocket, response: Response) {
        super.onOpen(webSocket, response)
        this._webSocket = webSocket
        _connected.value = true
        runMessages()
    }

    fun send(jsonObject: JSONObject) {
        _messageQueue.add(jsonObject.toString())
        runMessages()
    }

    private fun runMessages() {
        while (!_messageQueue.isEmpty() && _connected.value) {
            _messageQueue.poll()?.let { _webSocket.send(it) }
        }
    }

    companion object {
        const val ROUTEKEY = "route"
    }
}