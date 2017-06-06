package com.glodanif.bluetoothchat.model

import com.glodanif.bluetoothchat.entity.ChatMessage

interface MessagesStorage {

    fun setListener(listener: (List<ChatMessage>) -> Unit)
    fun getMessagesByDevice(address: String)
    fun insertMessage(message: ChatMessage)
}
