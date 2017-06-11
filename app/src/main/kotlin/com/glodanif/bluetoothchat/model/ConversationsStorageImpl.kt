package com.glodanif.bluetoothchat.model

import android.content.Context
import android.os.Handler
import com.glodanif.bluetoothchat.Storage
import com.glodanif.bluetoothchat.database.ConversationsDao
import com.glodanif.bluetoothchat.entity.Conversation
import kotlin.concurrent.thread

class ConversationsStorageImpl(context: Context) : ConversationsStorage {

    private val handler: Handler = Handler()
    val dao: ConversationsDao = Storage.getInstance(context).db.conversationsDao()

    override fun getConversations(listener: (List<Conversation>) -> Unit) {
        thread {
            val conversations: List<Conversation> = dao.getAllConversationsWithMessages()
            handler.post { listener.invoke(conversations) }
        }
    }

    override fun insertConversation(conversation: Conversation) {
        thread { dao.insert(conversation) }
    }
}
