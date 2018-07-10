package com.glodanif.bluetoothchat.data.model

import android.content.Context
import com.glodanif.bluetoothchat.data.database.Storage
import com.glodanif.bluetoothchat.data.entity.Conversation

class ConversationsStorageImpl(context: Context) : ConversationsStorage {

    private val dao = Storage.getInstance(context).db.conversationsDao()
    private val messageDao = Storage.getInstance(context).db.messagesDao()

    override suspend fun getContacts() = dao.getContacts()

    override suspend fun getConversations() = dao.getAllConversationsWithMessages()

    override suspend fun getConversationByAddress(address: String) = dao.getConversationByAddress(address)

    override suspend fun insertConversation(conversation: Conversation) {
        dao.insert(conversation)
    }

    override suspend fun removeConversationByAddress(address: String) {
        dao.delete(address)
        messageDao.deleteAllByDeviceAddress(address)
    }
}
