package com.glodanif.bluetoothchat.data.model

import android.content.Context
import android.os.Handler
import com.glodanif.bluetoothchat.data.database.Storage
import com.glodanif.bluetoothchat.data.database.MessagesDao
import com.glodanif.bluetoothchat.data.entity.ChatMessage
import kotlin.concurrent.thread

class MessagesStorageImpl(val context: Context) : MessagesStorage {

    private val handler: Handler = Handler()

    val dao: MessagesDao = Storage.getInstance(context).db.messagesDao()
    override fun insertMessage(message: ChatMessage) {
        thread { dao.insert(message) }
    }

    override fun getMessagesByDevice(address: String, listener: (List<ChatMessage>) -> Unit) {
        thread {
            val messages: List<ChatMessage> = dao.getMessagesByDevice(address)
            handler.post { listener.invoke(messages) }
        }
    }

    override fun updateMessages(messages: List<ChatMessage>) {
        thread { dao.updateMessages(messages) }
    }
}
