package com.glodanif.bluetoothchat.model

import com.glodanif.bluetoothchat.entity.Conversation

abstract class SimpleConnectionListener : OnConnectionListener {

    override fun onConnecting() {}

    override abstract fun onConnected()

    override fun onConnectedIn(conversation: Conversation) {}

    override fun onConnectedOut(conversation: Conversation) {}

    override abstract fun onConnectionLost()

    override abstract fun onConnectionFailed()

    override fun onConnectionDestroyed() {}

    override fun onDisconnected() {}

    override fun onConnectionAccepted() {}

    override fun onConnectionRejected() {}

    override fun onConnectionWithdrawn() {}
}