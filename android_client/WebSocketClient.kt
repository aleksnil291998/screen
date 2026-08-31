package com.example.screenstreamer

import okhttp3.*
import java.util.concurrent.TimeUnit

class WebSocketClient(private val url: String) {
    private var webSocket: WebSocket? = null
    private val client = OkHttpClient.Builder()
        .readTimeout(0, TimeUnit.MILLISECONDS)
        .pingInterval(30, TimeUnit.SECONDS)
        .build()
    
    private var connected = false
    
    fun connect() {
        val request = Request.Builder().url(url).build()
        val listener = object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                connected = true
                println("WebSocket connected")
            }
            
            override fun onMessage(webSocket: WebSocket, text: String) {
                // Text messages not expected
            }
            
            override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
                // Binary messages not expected from server
            }
            
            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                connected = false
                webSocket.close(1000, null)
                println("WebSocket closing: $code / $reason")
            }
            
            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                connected = false
                println("WebSocket closed: $code / $reason")
            }
            
            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                connected = false
                println("WebSocket error: ${t.message}")
            }
        }
        
        webSocket = client.newWebSocket(request, listener)
    }
    
    fun send(data: ByteArray) {
        webSocket?.send(ByteString.of(*data))
    }
    
    fun isConnected(): Boolean {
        return connected && webSocket != null
    }
    
    fun close() {
        webSocket?.close(1000, "Client closing")
        connected = false
    }
}
