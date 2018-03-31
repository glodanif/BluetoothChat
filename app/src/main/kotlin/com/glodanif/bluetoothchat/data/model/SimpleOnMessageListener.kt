package com.glodanif.bluetoothchat.data.model

abstract class SimpleOnMessageListener : OnMessageListener {

    override fun onMessageDelivered(id: String) {}

    override fun onMessageNotDelivered(id: String) {}

    override fun onMessageSeen(id: String) {}
}