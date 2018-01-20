package com.glodanif.bluetoothchat.data.model

import com.glodanif.bluetoothchat.data.entity.ChatMessage

interface MessagesStorage {

    fun getMessagesByDevice(address: String, listener: (List<ChatMessage>) -> Unit)
    fun getFilesMessagesByDevice(address: String?, listener: (List<ChatMessage>) -> Unit)
    fun insertMessage(message: ChatMessage)
    fun updateMessage(message: ChatMessage)
    fun updateMessages(messages: List<ChatMessage>)
}
