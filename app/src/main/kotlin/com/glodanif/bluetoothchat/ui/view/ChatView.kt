package com.glodanif.bluetoothchat.ui.view

import com.glodanif.bluetoothchat.ui.viewmodel.ChatMessageViewModel

interface ChatView {

    fun showMessagesHistory(messages: List<ChatMessageViewModel>)
    fun showReceivedMessage(message: ChatMessageViewModel)
    fun showSentMessage(message: ChatMessageViewModel)
    fun showServiceDestroyed()
    fun showRejectedConnection()
    fun showConnectionRequest(displayName: String, deviceName: String)
    fun showLostConnection()
    fun showFailedConnection()
    fun showDisconnected()
    fun showNotConnectedToAnyDevice()
    fun showNotConnectedToThisDevice(currentDevice: String)
    fun showNotValidMessage()
    fun showNotConnectedToSend()
    fun showReceiverUnableToReceiveImages()
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

    fun showPresharingImage(path: String)
    fun showImageTooBig(maxSize: Long)
    fun showImageNotExist()
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
