package com.glodanif.bluetoothchat.domain.interactor

import com.glodanif.bluetoothchat.data.entity.MessageFile
import com.glodanif.bluetoothchat.data.model.MessagesStorage
import java.io.File

class GetReceivedImagesInteractor(private val storage: MessagesStorage) : BaseInteractor<String, List<MessageFile>>() {

    override suspend fun execute(input: String): List<MessageFile> {

        val messages = if (input.isNotEmpty())
            storage.getFileMessagesByDevice(input) else storage.getAllFilesMessages()
        return messages.filter { File(it.filePath).exists() }
    }
}
