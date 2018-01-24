package com.glodanif.bluetoothchat.data.model

import android.content.Context
import android.os.Handler
import com.glodanif.bluetoothchat.data.database.Storage
import com.glodanif.bluetoothchat.data.entity.Conversation
import java.io.File
import kotlin.concurrent.thread

class ConversationsStorageImpl(context: Context) : ConversationsStorage {

    private val handler: Handler = Handler()
    private val dao = Storage.getInstance(context).db.conversationsDao()
    private val messageDao = Storage.getInstance(context).db.messagesDao()

    override fun getConversations(listener: (List<Conversation>) -> Unit) {
        thread {
            val conversations: List<Conversation> = dao.getAllConversationsWithMessages()
            handler.post { listener.invoke(conversations) }
        }
    }

    override fun getConversationByAddress(address: String, listener: (Conversation?) -> Unit) {
        thread {
            val conversation: Conversation? = dao.getConversationByAddress(address)
            handler.post { listener.invoke(conversation) }
        }
    }

    override fun insertConversation(conversation: Conversation) {
        thread { dao.insert(conversation) }
    }

    override fun removeConversation(conversation: Conversation) {

        thread {

            dao.delete(conversation)

            messageDao.getFilesMessagesByDevice(conversation.deviceAddress).forEach {
                if (it.filePath != null) {
                    val file = File(it.filePath)
                    file.delete()
                }
            }
            messageDao.deleteAllByDeviceAddress(conversation.deviceAddress)
        }
    }
}
