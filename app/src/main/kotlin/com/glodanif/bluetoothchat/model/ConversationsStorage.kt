package com.glodanif.bluetoothchat.model

import com.glodanif.bluetoothchat.entity.Conversation

interface ConversationsStorage {
    fun getConversations(listener: (List<Conversation>) -> Unit)
    fun insertConversation(conversation: Conversation)
    fun removeConversation(conversation: Conversation)
}
