package com.njbrady.nusic

import android.util.Log
import com.njbrady.nusic.utils.HttpOptions
import com.njbrady.nusic.utils.OnSocketRoute
import com.njbrady.nusic.utils.LocalStorage
import com.njbrady.nusic.utils.UrlProvider.Companion.baseWebSocketUrl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import okhttp3.*
import okio.ByteString
import org.json.JSONObject
import java.util.*


class MainSocketHandler(private val okHttpClient: OkHttpClient, private val localStorage: LocalStorage) : WebSocketListener() {

    private val _messageQueue = LinkedList<String>()
    private val _socketRoutes = mutableListOf<OnSocketRoute>()
    private val _connected = MutableStateFlow(true)
    private val _loadingConnection = MutableStateFlow(false)
    private lateinit var _webSocket: WebSocket

    val connected: StateFlow<Boolean> = _connected
    val loadingConnection: StateFlow<Boolean> = _loadingConnection

    init {
        Log.e("MAINSOCKETHANDLER", "socket handler created")
        openConnection()
    }

    private fun openConnection() {
        _loadingConnection.value = true
        val request = Request.Builder()
            .url(baseWebSocketUrl)
            .header(HttpOptions.Authorization, localStorage.retrieveToken())
            .build()

        _webSocket = okHttpClient.newWebSocket(request, this)
        _loadingConnection.value = false
    }

    fun disconnect() {
        _webSocket.close(1000, "Loggin out!")
    }

    fun retryConnection() {
        openConnection()
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
//        _connected.value = false
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