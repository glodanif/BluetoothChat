package com.glodanif.bluetoothchat.data.model

import android.content.Context
import android.os.Handler
import com.glodanif.bluetoothchat.data.database.Storage
import com.glodanif.bluetoothchat.data.entity.Conversation
import java.io.File
import kotlin.concurrent.thread

class ConversationsStorageImpl(context: Context) : ConversationsStorage {

    private val dao = Storage.getInstance(context).db.conversationsDao()
    private val messageDao = Storage.getInstance(context).db.messagesDao()

    override suspend fun getConversations(): List<Conversation> {
        return dao.getAllConversationsWithMessages()
    }

    override suspend fun getConversationByAddress(address: String): Conversation? {
        return dao.getConversationByAddress(address)
    }

    override suspend fun insertConversation(conversation: Conversation) {
        dao.insert(conversation)
    }

    override suspend fun removeConversationByAddress(address: String) {

        dao.delete(address)

        /*messageDao.getFileMessagesByDevice(address).forEach {
            if (it.filePath != null) {
                val file = File(it.filePath)
                file.delete()
            }
        }*/
        messageDao.deleteAllByDeviceAddress(address)
    }
}
