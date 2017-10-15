package com.glodanif.bluetoothchat.data.model

import com.glodanif.bluetoothchat.data.entity.Conversation

interface ConversationsStorage {
    fun getConversations(listener: (List<Conversation>) -> Unit)
    fun getConversationByAddress(address: String, listener: (Conversation?) -> Unit)
    fun insertConversation(conversation: Conversation)
    fun removeConversation(conversation: Conversation)
}
