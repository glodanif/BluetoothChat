package com.glodanif.bluetoothchat.model

import com.glodanif.bluetoothchat.entity.ChatMessage

interface MessagesStorage {

    fun getMessagesByDevice(address: String, listener: (List<ChatMessage>) -> Unit)
    fun insertMessage(message: ChatMessage)
    fun updateMessages(messages: List<ChatMessage>)
}
