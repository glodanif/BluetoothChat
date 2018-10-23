package com.glodanif.bluetoothchat.domain.interactor

import com.glodanif.bluetoothchat.data.entity.ChatMessage
import com.glodanif.bluetoothchat.data.model.MessagesStorage

class MarkMessagesAsSeenMessagesInteractor(private val messagesStorage: MessagesStorage) : BaseInteractor<List<ChatMessage>, List<ChatMessage>>() {

    override suspend fun execute(input: List<ChatMessage>): List<ChatMessage> {
        input.forEach { it.seenHere = true }
        messagesStorage.updateMessages(input)
        return input
    }
}
