package com.glodanif.bluetoothchat.ui.view

import com.glodanif.bluetoothchat.data.entity.ChatMessage
import com.glodanif.bluetoothchat.data.entity.Conversation

interface ChatView {

    fun showMessagesHistory(messages: List<ChatMessage>)
    fun showReceivedMessage(message: ChatMessage)
    fun showSentMessage(message: ChatMessage)
    fun showServiceDestroyed()
    fun showRejectedConnection()
    fun showConnectionRequest(conversation: Conversation)
    fun showLostConnection()
    fun showFailedConnection()
    fun showDisconnected()
    fun showNotConnectedToAnyDevice()
    fun showNotConnectedToThisDevice(currentDevice: String)
    fun showNotValidMessage()
    fun showNotConnectedToSend()
    fun showDeviceIsNotAvailable()
    fun showWainingForOpponent()
    fun hideActions()
    fun afterMessageSent()
    fun showStatusConnected()
    fun showStatusNotConnected()
    fun showStatusPending()
    fun showPartnerName(name: String, device: String)
    fun showBluetoothDisabled()
    fun showBluetoothEnablingFailed()
    fun requestBluetoothEnabling()
    fun dismissMessageNotification()

    fun pickImage()
    fun showImageTooBig(maxSize: Long)
    fun showImageTransferLayout(fileAddress: String?, fileSize: Long, transferType: FileTransferType)
    fun updateImageTransferProgress(transferredBytes: Long, totalBytes: Long)
    fun hideImageTransferLayout()
    fun showImageTransferCanceled()
    fun showImageTransferFailure()

    enum class FileTransferType {
        SENDING,
        RECEIVING
    }
}