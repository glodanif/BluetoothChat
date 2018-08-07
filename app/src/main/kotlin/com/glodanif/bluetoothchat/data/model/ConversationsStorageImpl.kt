package com.glodanif.bluetoothchat.data.model

import com.glodanif.bluetoothchat.data.database.ChatDatabase
import com.glodanif.bluetoothchat.data.entity.Conversation

class ConversationsStorageImpl(db: ChatDatabase) : ConversationsStorage {

    private val dao = db.conversationsDao()

    override suspend fun getContacts() = dao.getContacts()

    override suspend fun getConversations() = dao.getAllConversationsWithMessages()

    override suspend fun getConversationByAddress(address: String) = dao.getConversationByAddress(address)

    override suspend fun insertConversation(conversation: Conversation) {
        dao.insert(conversation)
    }

    override suspend fun removeConversationByAddress(address: String) {
        dao.delete(address)
    }
}
