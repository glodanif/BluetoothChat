package com.glodanif.bluetoothchat.domain.interactor

import com.glodanif.bluetoothchat.data.entity.ConversationWithMessages
import com.glodanif.bluetoothchat.data.entity.MessageFile
import com.glodanif.bluetoothchat.data.model.ConversationsStorage
import com.glodanif.bluetoothchat.data.model.MessagesStorage

class GetConversationsInteractor(private val storage: ConversationsStorage): BaseInteractor<Unit, List<ConversationWithMessages>>() {

    override suspend fun execute(input: Unit) = storage.getConversations()
}
