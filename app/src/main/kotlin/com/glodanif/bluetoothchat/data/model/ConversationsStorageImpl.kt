package com.glodanif.bluetoothchat.data.model

import com.glodanif.bluetoothchat.data.database.ChatDatabase
import com.glodanif.bluetoothchat.data.entity.Conversation

class ConversationsStorageImpl(db: ChatDatabase) : ConversationsStorage {

    private val conversationDao = db.conversationsDao()
    private val messageDao = db.messagesDao()

    override suspend fun getContacts() = conversationDao.getContacts()

    override suspend fun getConversations() = conversationDao.getAllConversationsWithMessages()

    override suspend fun getConversationByAddress(address: String) = conversationDao.getConversationByAddress(address)

    override suspend fun insertConversation(conversation: Conversation) {
        conversationDao.insert(conversation)
    }

    override suspend fun removeConversationByAddress(address: String) {
        conversationDao.delete(address)
        messageDao.deleteAllByDeviceAddress(address)
    }
}
