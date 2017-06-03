package com.glodanif.bluetoothchat.model

import android.bluetooth.BluetoothDevice
import com.glodanif.bluetoothchat.entity.ChatMessage

interface BluetoothConnector {

    fun prepare()
    fun release()
    fun stop()
    fun restart()
    fun setOnConnectListener(listener: OnConnectListener)
    fun setOnPrepareListener(listener: OnPrepareListener)
    fun setOnMessageListener(listener: OnMessageListener)
    fun connect(device: BluetoothDevice)
    fun sendMessage(message: String)
    fun isConnected(): Boolean

    interface OnConnectListener {
        fun onConnecting()
        fun onConnectedIn(device: BluetoothDevice)
        fun onConnectedOut(device: BluetoothDevice)
        fun onConnectionLost()
        fun onConnectionFailed()
        fun onDisconnected()
    }

    interface OnMessageListener {
        fun onMessageReceived(message: ChatMessage)
        fun onMessageSent(message: ChatMessage)
    }

    interface OnPrepareListener {
        fun onPrepared()
        fun onError()
    }
}