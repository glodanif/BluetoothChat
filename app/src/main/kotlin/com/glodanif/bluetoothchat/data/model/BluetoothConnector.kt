package com.glodanif.bluetoothchat.data.model

import android.bluetooth.BluetoothDevice
import com.glodanif.bluetoothchat.data.entity.Conversation

interface BluetoothConnector {

    fun prepare()
    fun release()
    fun stop()
    fun restart()
    fun setOnConnectListener(listener: OnConnectionListener?)
    fun setOnPrepareListener(listener: OnPrepareListener?)
    fun setOnMessageListener(listener: OnMessageListener?)
    fun connect(device: BluetoothDevice)
    fun sendMessage(message: String)
    fun isConnected(): Boolean
    fun isConnectedOrPending(): Boolean
    fun isPending(): Boolean
    fun isConnectionPrepared(): Boolean
    fun getCurrentConversation(): Conversation?
    fun acceptConnection()
    fun rejectConnection()
    fun sendDisconnectRequest()
}
