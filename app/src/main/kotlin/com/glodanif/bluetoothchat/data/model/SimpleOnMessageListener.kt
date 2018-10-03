package com.glodanif.bluetoothchat.data.model

abstract class SimpleOnMessageListener : OnMessageListener {

    override fun onMessageSendingFailed() {}

    override fun onMessageDelivered(id: Long) {}

    override fun onMessageNotDelivered(id: Long) {}

    override fun onMessageSeen(id: Long) {}
}