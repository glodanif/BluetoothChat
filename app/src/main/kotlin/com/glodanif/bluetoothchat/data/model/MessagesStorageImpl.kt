package com.glodanif.bluetoothchat.data.model

import com.glodanif.bluetoothchat.data.database.ChatDatabase
import com.glodanif.bluetoothchat.data.entity.ChatMessage
import com.glodanif.bluetoothchat.data.entity.MessageFile
import java.io.File

class MessagesStorageImpl(db: ChatDatabase) : MessagesStorage {

    private val dao = db.messagesDao()

    override suspend fun insertMessage(message: ChatMessage) {
        dao.insert(message)
    }

    override suspend fun getMessagesByDevice(address: String): List<ChatMessage> {
        return dao.getMessagesByDevice(address)
    }

    override suspend fun getFileMessageById(uid: Long): MessageFile? {
        return dao.getFileMessageById(uid)
    }

    override suspend fun getFileMessagesByDevice(address: String): List<MessageFile> {
        return dao.getFileMessagesByDevice(address)
    }

    override suspend fun getAllFilesMessages(): List<MessageFile> {
        return dao.getAllFilesMessages()
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

    override suspend fun removeMessagesByAddress(address: String) {
        dao.deleteAllByDeviceAddress(address)
    }

    override suspend fun setMessageAsDelivered(messageId: Long) {
        dao.setMessageAsDelivered(messageId)
    }

    override suspend fun setMessageAsSeenThere(messageId: Long) {
        dao.setMessageAsSeenThere(messageId)
    }
}
