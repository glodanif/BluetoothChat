package com.glodanif.bluetoothchat.domain.interactor

import com.glodanif.bluetoothchat.data.entity.Conversation
import com.glodanif.bluetoothchat.data.model.ConversationsStorage

class GetContactsInteractor(private val storage: ConversationsStorage): BaseInteractor<Unit, List<Conversation>>() {

    override suspend fun execute(input: Unit) = storage.getContacts()
}
