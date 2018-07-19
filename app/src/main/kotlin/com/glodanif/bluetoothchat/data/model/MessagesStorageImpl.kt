package com.glodanif.bluetoothchat.data.model

import com.glodanif.bluetoothchat.data.database.ChatDatabase
import com.glodanif.bluetoothchat.data.database.MessagesDao
import com.glodanif.bluetoothchat.data.entity.ChatMessage
import com.glodanif.bluetoothchat.data.entity.MessageFile
import java.io.File

class MessagesStorageImpl(db: ChatDatabase) : MessagesStorage {

    private val dao: MessagesDao = db.messagesDao()

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

    override suspend fun getFileMessageById(uid: Long): MessageFile? {
        return dao.getFileMessageById(uid)
    }

    override suspend fun getFileMessagesByDevice(address: String?): List<MessageFile> {
        return (if (address != null)
            dao.getFileMessagesByDevice(address) else dao.getAllFilesMessages())
                .filter { File(it.filePath).exists() }
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
