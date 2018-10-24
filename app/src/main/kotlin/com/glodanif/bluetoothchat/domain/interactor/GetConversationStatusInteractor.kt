package com.glodanif.bluetoothchat.domain.interactor

import com.glodanif.bluetoothchat.data.model.BluetoothConnector
import com.glodanif.bluetoothchat.domain.ConversationStatus

class GetConversationStatusInteractor(private val connection: BluetoothConnector) : BaseInteractor<String, ConversationStatus>() {

    override suspend fun execute(input: String): ConversationStatus {

        val conversation = connection.getCurrentConversation()

        return if (conversation == null) {
            if (connection.isPending()) {
                ConversationStatus.Pending
            } else {
                ConversationStatus.NotConnected
            }
        } else if (conversation.deviceAddress != input) {
            ConversationStatus.ConnectedToOther(conversation.displayName, conversation.deviceName)
        } else if (connection.isPending() && conversation.deviceAddress == input) {
            ConversationStatus.IncomingRequest(conversation.displayName, conversation.deviceName)
        } else {
            ConversationStatus.Connected
        }
    }
}
