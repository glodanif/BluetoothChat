package com.glodanif.bluetoothchat.data.model

import com.glodanif.bluetoothchat.data.entity.ChatMessage

interface OnMessageListener {
    fun onMessageReceived(message: ChatMessage)
    fun onMessageSent(message: ChatMessage)
    fun onMessageSendingFailed()
    fun onMessageDelivered(id: Long)
    fun onMessageNotDelivered(id: Long)
    fun onMessageSeen(id: Long)
}