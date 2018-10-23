package com.glodanif.bluetoothchat.domain.interactor

import com.glodanif.bluetoothchat.data.entity.ChatMessage
import com.glodanif.bluetoothchat.data.model.MessagesStorage
import java.io.File

class GetMessagesByAddressInteractor(private val storage: MessagesStorage) : BaseInteractor<String, List<ChatMessage>>() {

    override suspend fun execute(input: String): List<ChatMessage> {

        val messages = storage.getMessagesByDevice(input)
        messages.forEach {
            if (it.filePath != null) {
                it.fileExists = File(it.filePath).exists()
            }
        }
        return messages
    }
}
