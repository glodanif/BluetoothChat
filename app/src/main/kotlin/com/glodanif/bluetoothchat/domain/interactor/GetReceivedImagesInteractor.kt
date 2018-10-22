package com.glodanif.bluetoothchat.domain.interactor

import com.glodanif.bluetoothchat.data.entity.MessageFile
import com.glodanif.bluetoothchat.data.model.MessagesStorage

class GetReceivedImagesInteractor(private val storage: MessagesStorage): BaseInteractor<String, List<MessageFile>>() {

    override suspend fun execute(input: String) = storage.getFileMessagesByDevice(input)
}
