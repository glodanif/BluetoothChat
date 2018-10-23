package com.glodanif.bluetoothchat.domain.interactor

import com.glodanif.bluetoothchat.data.entity.Conversation
import com.glodanif.bluetoothchat.data.entity.ConversationWithMessages
import com.glodanif.bluetoothchat.data.entity.MessageFile
import com.glodanif.bluetoothchat.data.model.ConversationsStorage
import com.glodanif.bluetoothchat.data.model.MessagesStorage

class GetConversationByAddressInteractor(private val storage: ConversationsStorage) : BaseInteractor<String, Conversation?>() {

    override suspend fun execute(input: String): Conversation? = storage.getConversationByAddress(input)
}
