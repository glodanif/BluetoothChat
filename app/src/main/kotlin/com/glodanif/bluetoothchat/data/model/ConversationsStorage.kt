package com.glodanif.bluetoothchat.data.model

import com.glodanif.bluetoothchat.data.entity.Conversation

interface ConversationsStorage {
    suspend fun getConversations(): List<Conversation>
    suspend fun getConversationByAddress(address: String): Conversation?
    suspend fun insertConversation(conversation: Conversation)
    suspend fun removeConversation(conversation: Conversation)
}
