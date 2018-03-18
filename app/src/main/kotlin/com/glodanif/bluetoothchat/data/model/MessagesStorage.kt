package com.glodanif.bluetoothchat.data.model

import com.glodanif.bluetoothchat.data.entity.ChatMessage

interface MessagesStorage {
    suspend fun getMessagesByDevice(address: String): List<ChatMessage>
    suspend fun getFileMessagesByDevice(address: String?): List<ChatMessage>
    suspend fun insertMessage(message: ChatMessage)
    suspend fun updateMessage(message: ChatMessage)
    suspend fun updateMessages(messages: List<ChatMessage>)
}
