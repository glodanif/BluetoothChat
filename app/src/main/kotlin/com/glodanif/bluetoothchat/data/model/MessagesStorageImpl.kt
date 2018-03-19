package com.glodanif.bluetoothchat.data.model

import android.content.Context
import android.os.Handler
import com.glodanif.bluetoothchat.data.database.Storage
import com.glodanif.bluetoothchat.data.database.MessagesDao
import com.glodanif.bluetoothchat.data.entity.ChatMessage
import java.io.File
import kotlin.concurrent.thread

class MessagesStorageImpl(val context: Context) : MessagesStorage {

    private val dao: MessagesDao = Storage.getInstance(context).db.messagesDao()

    override suspend fun insertMessage(message: ChatMessage) {
        dao.insert(message)
    }

    override suspend fun getMessagesByDevice(address: String): List<ChatMessage> {
        val messages = dao.getMessagesByDevice(address)
        messages.forEach {
            if (it.filePath != null) {
                it.fileExists = File(it.filePath).exists()
            }
        }
        return messages
    }

    override suspend fun getMessageById(uid: Long): ChatMessage? {
        return dao.getFileMessageById(uid)
    }

    override suspend fun getFileMessagesByDevice(address: String?): List<ChatMessage> {
        return (if (address != null)
            dao.getFileMessagesByDevice(address) else dao.getAllFilesMessages())
                .filter { !it.filePath.isNullOrEmpty() && File(it.filePath).exists() }
    }

    override suspend fun updateMessage(message: ChatMessage) {
        dao.updateMessage(message)
    }

    override suspend fun updateMessages(messages: List<ChatMessage>) {
        dao.updateMessages(messages)
    }

    override suspend fun removeFileInfo(messageId: Long) {
        dao.removeFileInfo(messageId)
    }
}
