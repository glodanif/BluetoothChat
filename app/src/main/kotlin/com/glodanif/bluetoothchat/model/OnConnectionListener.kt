package com.glodanif.bluetoothchat.model

import com.glodanif.bluetoothchat.entity.Conversation

interface OnConnectionListener {
    fun onConnecting()
    fun onConnected()
    fun onConnectedIn(conversation: Conversation)
    fun onConnectedOut(conversation: Conversation)
    fun onConnectionLost()
    fun onConnectionFailed()
    fun onConnectionDestroyed()
    fun onDisconnected()
    fun onConnectionAccepted()
    fun onConnectionRejected()
    fun onConnectionWithdrawn()
}