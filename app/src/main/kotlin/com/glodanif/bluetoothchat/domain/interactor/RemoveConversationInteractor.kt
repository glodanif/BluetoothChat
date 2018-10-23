package com.glodanif.bluetoothchat.domain.interactor

import com.glodanif.bluetoothchat.data.model.ConversationsStorage
import com.glodanif.bluetoothchat.data.model.MessagesStorage

class RemoveConversationInteractor(private val messages: MessagesStorage, private val conversations: ConversationsStorage): BaseInteractor<String, Unit>() {

    override suspend fun execute(input: String) {
        conversations.removeConversationByAddress(input)
        messages.removeMessagesByAddress(input)
    }
}
