package com.glodanif.bluetoothchat.model

import android.content.Context
import android.os.Handler
import com.glodanif.bluetoothchat.Storage
import com.glodanif.bluetoothchat.database.MessagesDao
import com.glodanif.bluetoothchat.entity.ChatMessage
import kotlin.concurrent.thread

class MessagesStorageImpl(val context: Context) : MessagesStorage {

    private val handler: Handler = Handler()
    private var listener: ((List<ChatMessage>) -> Unit)? = null
    val messagesDao: MessagesDao = Storage.getInstance(context).db.messagesDao()

    override fun setListener(listener: (List<ChatMessage>) -> Unit) {
        this.listener = listener
    }

    override fun insertMessage(message: ChatMessage) {
        thread { messagesDao.insert(message) }
    }

    override fun getMessagesByDevice(address: String) {
        thread {
            val messages: List<ChatMessage> = messagesDao.getMessagesByDevice(address)
            handler.post { listener?.invoke(messages) }
        }
    }
}
