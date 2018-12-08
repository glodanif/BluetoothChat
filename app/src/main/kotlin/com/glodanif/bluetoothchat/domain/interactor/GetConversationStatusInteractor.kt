package com.glodanif.bluetoothchat.domain.interactor

import com.glodanif.bluetoothchat.data.model.BluetoothConnector
import com.glodanif.bluetoothchat.domain.ConversationStatus

class GetConversationStatusInteractor(private val connection: BluetoothConnector) {

    fun execute(input: String, onResult: ((ConversationStatus) -> Unit)? = null) {

        val conversation = connection.getCurrentConversation()
        if (conversation == null) {
            if (connection.isPending()) {
                onResult?.invoke(ConversationStatus.Pending)
            } else {
                onResult?.invoke(ConversationStatus.NotConnected)
            }
        } else if (conversation.deviceAddress != input) {
            onResult?.invoke(ConversationStatus.ConnectedToOther(conversation.displayName, conversation.deviceName))
        } else if (connection.isPending()) {
            onResult?.invoke(ConversationStatus.IncomingRequest(conversation.displayName, conversation.deviceName))
        } else {
            onResult?.invoke(ConversationStatus.Connected)
        }
    }
}
