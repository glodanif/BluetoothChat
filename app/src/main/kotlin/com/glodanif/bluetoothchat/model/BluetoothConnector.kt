package com.glodanif.bluetoothchat.model

import android.bluetooth.BluetoothDevice

interface BluetoothConnector {

    fun prepare()
    fun release()
    fun stop()
    fun restart()
    fun setOnConnectListener(listener: OnConnectionListener)
    fun setOnPrepareListener(listener: OnPrepareListener)
    fun setOnMessageListener(listener: OnMessageListener)
    fun connect(device: BluetoothDevice)
    fun sendMessage(message: String)
    fun isConnected(): Boolean
}
