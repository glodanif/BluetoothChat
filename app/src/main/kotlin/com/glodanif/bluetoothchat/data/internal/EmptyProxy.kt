package com.glodanif.bluetoothchat.data.internal

import android.bluetooth.BluetoothDevice
import com.glodanif.bluetoothchat.data.entity.ChatMessage
import com.glodanif.bluetoothchat.data.entity.Conversation

class EmptyProxy : CommunicationProxy {
    override fun onConnecting() {}
    override fun onConnected(device: BluetoothDevice) {}
    override fun onConnectedIn(conversation: Conversation) {}
    override fun onConnectedOut(conversation: Conversation) {}
    override fun onConnectionLost() {}
    override fun onConnectionFailed() {}
    override fun onConnectionDestroyed() {}
    override fun onDisconnected() {}
    override fun onConnectionAccepted() {}
    override fun onConnectionRejected() {}
    override fun onConnectionWithdrawn() {}
    override fun onMessageReceived(message: ChatMessage) {}
    override fun onMessageSent(message: ChatMessage) {}
    override fun onMessageSendingFailed() {}
    override fun onMessageDelivered(id: Long) {}
    override fun onMessageNotDelivered(id: Long) {}
    override fun onMessageSeen(id: Long) {}
    override fun onFileSendingStarted(fileAddress: String?, fileSize: Long) {}
    override fun onFileSendingProgress(sentBytes: Long, totalBytes: Long) {}
    override fun onFileSendingFinished() {}
    override fun onFileSendingFailed() {}
    override fun onFileReceivingStarted(fileSize: Long) {}
    override fun onFileReceivingProgress(sentBytes: Long, totalBytes: Long) {}
    override fun onFileReceivingFinished() {}
    override fun onFileReceivingFailed() {}
    override fun onFileTransferCanceled(byPartner: Boolean) {}
}
