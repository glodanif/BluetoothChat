package com.glodanif.bluetoothchat.ui.presenter

import android.bluetooth.BluetoothDevice
import android.os.Handler
import android.util.Log
import com.glodanif.bluetoothchat.data.entity.ChatMessage
import com.glodanif.bluetoothchat.data.entity.Conversation
import com.glodanif.bluetoothchat.data.entity.TransferringFile
import com.glodanif.bluetoothchat.data.model.*
import com.glodanif.bluetoothchat.di.ComponentsManager
import com.glodanif.bluetoothchat.ui.view.ChatView
import kotlinx.coroutines.experimental.launch
import java.io.File
import javax.inject.Inject

class ChatPresenter(private val deviceAddress: String, private val view: ChatView, private val scanModel: BluetoothScanner,
                    private val connectionModel: BluetoothConnector) {

    @Inject
    lateinit var conversationsStorage: ConversationsStorage
    @Inject
    lateinit var messagesStorage: MessagesStorage

    init {
        ComponentsManager.getDataSourceComponent().inject(this)
    }

    private val handler = Handler()

    private val maxFileSize = 5_242_880

    private var fileToSend: File? = null

    private val prepareListener = object : OnPrepareListener {

        override fun onPrepared() {
            connectionModel.setOnConnectListener(connectionListener)
            connectionModel.setOnMessageListener(messageListener)
            connectionModel.setOnFileListener(fileListener)
            updateState()
            dismissNotification()

            if (fileToSend != null) {

                if (fileToSend!!.length() > maxFileSize) {
                    view.showImageTooBig(maxFileSize.toLong())
                } else {
                    connectionModel.sendFile(fileToSend!!)
                }
                fileToSend = null
            }
        }

        override fun onError() {
            releaseConnection()
        }
    }

    private val connectionListener = object : OnConnectionListener {

        override fun onConnected(device: BluetoothDevice) {

        }

        override fun onConnectionWithdrawn() {
            updateState()
        }

        override fun onConnectionDestroyed() {
            view.showServiceDestroyed()
        }

        override fun onConnectionAccepted() {
            view.showStatusConnected()
            view.hideActions()

            launch {
                val conversation = conversationsStorage.getConversationByAddress(deviceAddress)
                if (conversation != null) {
                    handler.post {
                        view.showPartnerName(conversation.displayName, conversation.deviceName)
                    }
                }
            }
        }

        override fun onConnectionRejected() {
            view.showRejectedConnection()
            updateState()
        }

        override fun onConnectedIn(conversation: Conversation) {
            val currentConversation: Conversation? = connectionModel.getCurrentConversation()
            if (currentConversation?.deviceAddress == deviceAddress) {
                view.showStatusPending()
                view.showConnectionRequest(conversation)
            }
        }

        override fun onConnectedOut(conversation: Conversation) {
        }

        override fun onConnecting() {
        }

        override fun onConnectionLost() {
            view.showLostConnection()
            updateState()
        }

        override fun onConnectionFailed() {
            view.showFailedConnection()
            updateState()
        }

        override fun onDisconnected() {
            view.showDisconnected()
            updateState()
        }
    }

    private val messageListener = object : OnMessageListener {

        override fun onMessageReceived(message: ChatMessage) {
            view.showReceivedMessage(message)
        }

        override fun onMessageSent(message: ChatMessage) {
            view.showSentMessage(message)
        }

        override fun onMessageDelivered(id: String) {

        }

        override fun onMessageNotDelivered(id: String) {

        }

        override fun onMessageSeen(id: String) {

        }
    }

    private val fileListener = object : OnFileListener {

        override fun onFileSendingStarted(fileAddress: String?, fileSize: Long) {
            view.showImageTransferLayout(fileAddress, fileSize, ChatView.FileTransferType.SENDING)
        }

        override fun onFileSendingProgress(sentBytes: Long, totalBytes: Long) {
            view.updateImageTransferProgress(sentBytes, totalBytes)
        }

        override fun onFileSendingFinished() {
            view.hideImageTransferLayout()
        }

        override fun onFileSendingFailed() {
            view.hideImageTransferLayout()
            view.showImageTransferFailure()
        }

        override fun onFileReceivingStarted(fileSize: Long) {
            view.showImageTransferLayout(null, fileSize, ChatView.FileTransferType.RECEIVING)
        }

        override fun onFileReceivingProgress(sentBytes: Long, totalBytes: Long) {
            view.updateImageTransferProgress(sentBytes, totalBytes)
        }

        override fun onFileReceivingFinished() {
            view.hideImageTransferLayout()
        }

        override fun onFileReceivingFailed() {
            view.hideImageTransferLayout()
            view.showImageTransferFailure()
        }

        override fun onFileTransferCanceled(byPartner: Boolean) {
            view.hideImageTransferLayout()
            if (byPartner) {
                view.showImageTransferCanceled()
            }
        }
    }

    private fun dismissNotification() {
        val currentConversation: Conversation? = connectionModel.getCurrentConversation()
        if (currentConversation != null && connectionModel.isConnectedOrPending() &&
                currentConversation.deviceAddress == deviceAddress) {
            view.dismissMessageNotification()
        }
    }

    fun prepareConnection() {

        if (!scanModel.isBluetoothEnabled()) {
            view.showBluetoothDisabled()
        } else {

            connectionModel.setOnPrepareListener(prepareListener)

            if (connectionModel.isConnectionPrepared()) {
                connectionModel.setOnConnectListener(connectionListener)
                connectionModel.setOnMessageListener(messageListener)
                connectionModel.setOnFileListener(fileListener)
                updateState()
                dismissNotification()

                if (fileToSend != null) {

                    if (fileToSend!!.length() > maxFileSize) {
                        view.showImageTooBig(maxFileSize.toLong())
                    } else {
                        connectionModel.sendFile(fileToSend!!)
                    }
                    fileToSend = null
                }
            } else {
                connectionModel.prepare()
            }
        }

        launch {

            val messages = messagesStorage.getMessagesByDevice(deviceAddress)
            val conversation = conversationsStorage.getConversationByAddress(deviceAddress)

            handler.post {
                messages.forEach { it.seenHere = true }
                view.showMessagesHistory(messages)
                if (conversation != null) {
                    view.showPartnerName(conversation.displayName, conversation.deviceName)
                }
            }

            messagesStorage.updateMessages(messages)
        }
    }

    fun releaseConnection() {

        connectionModel.setOnPrepareListener(null)
        connectionModel.setOnConnectListener(null)
        connectionModel.setOnMessageListener(null)
        connectionModel.setOnFileListener(null)

        if (!connectionModel.isConnectedOrPending()) {
            connectionModel.release()
        }
    }

    fun resetConnection() {
        connectionModel.restart()
        view.showStatusNotConnected()
        view.showNotConnectedToAnyDevice()
    }

    fun disconnect() {
        connectionModel.sendDisconnectRequest()
        view.showStatusNotConnected()
        view.showNotConnectedToAnyDevice()
    }

    fun connectToDevice() {

        val device = scanModel.getDeviceByAddress(deviceAddress)

        if (device != null) {
            view.showStatusPending()
            connectionModel.connect(device)
            view.showWainingForOpponent()
        } else {
            view.showStatusNotConnected()
            view.showDeviceIsNotAvailable()
        }
    }

    fun sendMessage(message: String) {

        if (!connectionModel.isConnected()) {
            view.showNotConnectedToSend()
            return
        }

        if (message.isEmpty()) {
            view.showNotValidMessage()
        } else {
            connectionModel.sendMessage(message)
            view.afterMessageSent()
        }
    }

    fun sendFile(file: File) {

        if (!connectionModel.isConnected()) {
            view.showNotConnectedToSend()
        } else {
            fileToSend = file
        }
    }

    fun pickImage() {

        val currentConversation: Conversation? = connectionModel.getCurrentConversation()

        if (!connectionModel.isConnected()) {
            view.showNotConnectedToSend()
        } else if (currentConversation != null && currentConversation.messageContractVersion < 1) {
            view.showReceiverUnableToReceiveImages()
        } else {
            view.pickImage()
        }
    }

    fun cancelFileTransfer() {
        connectionModel.cancelFileTransfer()
        view.hideImageTransferLayout()
    }

    fun reconnect() {

        if (scanModel.isBluetoothEnabled()) {
            connectToDevice()
            view.showStatusPending()
            view.showWainingForOpponent()
        } else {
            view.showBluetoothDisabled()
        }
    }

    fun acceptConnection() {
        view.hideActions()
        view.showStatusConnected()
        connectionModel.acceptConnection()
    }

    fun rejectConnection() {
        view.hideActions()
        view.showStatusNotConnected()
        connectionModel.rejectConnection()
        updateState()
    }

    fun onBluetoothEnabled() {
        prepareConnection()
    }

    fun onBluetoothEnablingFailed() {
        view.showBluetoothEnablingFailed()
    }

    fun enableBluetooth() {
        view.requestBluetoothEnabling()
    }

    private fun updateState() {

        val transferringFile = connectionModel.getTransferringFile()
        if (transferringFile != null) {
            val type = if (transferringFile.transferType == TransferringFile.TransferType.RECEIVING)
                ChatView.FileTransferType.RECEIVING else ChatView.FileTransferType.SENDING
            view.showImageTransferLayout(transferringFile.name, transferringFile.size, type)
        } else {
            view.hideImageTransferLayout()
        }

        val currentConversation: Conversation? = connectionModel.getCurrentConversation()

        if (currentConversation == null) {
            if (connectionModel.isPending()) {
                view.showStatusPending()
                view.showWainingForOpponent()
            } else {
                view.showStatusNotConnected()
                view.showNotConnectedToAnyDevice()
            }
        } else if (currentConversation.deviceAddress != deviceAddress) {
            view.showStatusNotConnected()
            view.showNotConnectedToThisDevice("${currentConversation.displayName} (${currentConversation.deviceName})")
        } else if (connectionModel.isPending() && currentConversation.deviceAddress == deviceAddress) {
            view.showStatusPending()
            view.showConnectionRequest(currentConversation)
        } else {
            view.showStatusConnected()
        }
    }
}
