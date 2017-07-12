package com.glodanif.bluetoothchat.model

import android.content.Context
import android.os.Handler
import android.util.Log
import com.glodanif.bluetoothchat.Storage
import com.glodanif.bluetoothchat.database.ConversationsDao
import com.glodanif.bluetoothchat.entity.Conversation
import kotlin.concurrent.thread

class ConversationsStorageImpl(context: Context) : ConversationsStorage {

    private val handler: Handler = Handler()
    val dao = Storage.getInstance(context).db.conversationsDao()
    val messageDao = Storage.getInstance(context).db.messagesDao()

    override fun getConversations(listener: (List<Conversation>) -> Unit) {
        thread {
            val conversations: List<Conversation> = dao.getAllConversationsWithMessages()
            handler.post { listener.invoke(conversations) }
        }
    }

    override fun insertConversation(conversation: Conversation) {
        thread { dao.insert(conversation) }
    }

    override fun removeConversation(conversation: Conversation) {
        thread {
            dao.delete(conversation)
            messageDao.deleteAllByDeviceAddress(conversation.deviceAddress)
        }
    }
}
