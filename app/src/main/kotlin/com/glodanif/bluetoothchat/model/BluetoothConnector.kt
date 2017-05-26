package com.glodanif.bluetoothchat.model

interface BluetoothConnector {

    fun prepare()
    fun release()
    fun prepareForAccept()
    fun stop()
    fun setOnConnectListener(listener: OnConnectListener)
    fun setOnPrepareListener(listener: OnPrepareListener)
    fun setOnMessageListener(listener: OnMessageListener)
    fun connect()
    fun sendMessage(message: String)

    interface OnConnectListener {
        fun onConnecting()
        fun onConnected(name: String)
        fun onConnectionLost()
        fun onDisconnected()
    }

    interface OnMessageListener {
        fun onMessageReceived(message: String)
        fun onMessageSent(id: Int)
    }

    interface OnPrepareListener {
        fun onPrepared()
        fun onError()
    }
}