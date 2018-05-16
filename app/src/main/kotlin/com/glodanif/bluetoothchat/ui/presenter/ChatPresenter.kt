package com.glodanif.bluetoothchat.ui.presenter

import android.bluetooth.BluetoothDevice
import com.glodanif.bluetoothchat.data.entity.ChatMessage
import com.glodanif.bluetoothchat.data.entity.Conversation
import com.glodanif.bluetoothchat.data.entity.MessageType
import com.glodanif.bluetoothchat.data.entity.TransferringFile
import com.glodanif.bluetoothchat.data.model.*
import com.glodanif.bluetoothchat.ui.view.ChatView
import com.glodanif.bluetoothchat.ui.viewmodel.converter.ChatMessageConverter
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch
import java.io.File
import kotlin.coroutines.experimental.CoroutineContext

class ChatPresenter(private val deviceAddress: String,
                    private val view: ChatView,
                    private val conversationsStorage: ConversationsStorage,
                    private val messagesStorage: MessagesStorage,
                    private val scanModel: BluetoothScanner,
                    private val connectionModel: BluetoothConnector,
                    private val preferences: UserPreferences,
                    private val converter: ChatMessageConverter,
                    private val uiContext: CoroutineContext = UI,
                    private val bgContext: CoroutineContext = CommonPool) {

    private val maxFileSize = 5_242_880

    private var fileToSend: File? = null
    private var filePresharing: File? = null

    private val prepareListener = object : OnPrepareListener {

        override fun onPrepared() {

            with(connectionModel) {
                setOnConnectListener(connectionListener)
                setOnMessageListener(messageListener)
                setOnFileListener(fileListener)
            }
            updateState()
            dismissNotification()

            fileToSend?.let {

                if (it.length() > maxFileSize) {
                    view.showImageTooBig(maxFileSize.toLong())
                } else {
                    connectionModel.sendFile(it, MessageType.IMAGE)
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

            launch(uiContext) {
                val conversation = async(bgContext) { conversationsStorage.getConversationByAddress(deviceAddress) }.await()
                if (conversation != null) {
                    view.showPartnerName(conversation.displayName, conversation.deviceName)
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
                view.showConnectionRequest(conversation.displayName, conversation.deviceName)
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
            view.showReceivedMessage(converter.transform(message))
        }

        override fun onMessageSent(message: ChatMessage) {
            view.showSentMessage(converter.transform(message))
        }

        override fun onMessageDelivered(id: Long) {

        }

        override fun onMessageNotDelivered(id: Long) {

        }

        override fun onMessageSeen(id: Long) {

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

        connectionModel.getCurrentConversation()?.let {
            if (connectionModel.isConnectedOrPending() && it.deviceAddress == deviceAddress) {
                view.dismissMessageNotification()
            }
        }
    }

    fun onViewCreated() {
        view.setBackgroundColor(preferences.getChatBackgroundColor())
    }

    fun prepareConnection() {

        if (!scanModel.isBluetoothEnabled()) {
            view.showBluetoothDisabled()
        } else {

            connectionModel.setOnPrepareListener(prepareListener)

            if (connectionModel.isConnectionPrepared()) {
                with(connectionModel) {
                    setOnConnectListener(connectionListener)
                    setOnMessageListener(messageListener)
                    setOnFileListener(fileListener)
                }
                updateState()
                dismissNotification()
                sendFileIfPrepared()
            } else {
                connectionModel.prepare()
            }
        }

        launch(uiContext) {
            val messagesDef = async(bgContext) { messagesStorage.getMessagesByDevice(deviceAddress) }
            val conversationDef = async(bgContext) { conversationsStorage.getConversationByAddress(deviceAddress) }
            displayInfo(messagesDef.await(), conversationDef.await())
        }
    }

    private fun sendFileIfPrepared() {

        fileToSend?.let {

            if (it.length() > maxFileSize) {
                view.showImageTooBig(maxFileSize.toLong())
            } else {
                connectionModel.sendFile(it, MessageType.IMAGE)
            }
            fileToSend = null
        }
    }

    private fun displayInfo(messages: List<ChatMessage>, partner: Conversation?) {

        messages.forEach { it.seenHere = true }
        view.showMessagesHistory(converter.transform(messages))
        if (partner != null) {
            view.showPartnerName(partner.displayName, partner.deviceName)
        }

        launch(bgContext) {
            messagesStorage.updateMessages(messages)
        }
    }

    fun releaseConnection() = with(connectionModel) {

        setOnPrepareListener(null)
        setOnConnectListener(null)
        setOnMessageListener(null)
        setOnFileListener(null)

        if (!isConnectedOrPending()) {
            release()
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

        scanModel.getDeviceByAddress(deviceAddress).let {

            if (it != null) {
                view.showStatusPending()
                view.showWainingForOpponent()
                connectionModel.connect(it)
            } else {
                view.showStatusNotConnected()
                view.showDeviceIsNotAvailable()
            }
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

        if (!file.exists()) {
            view.showImageNotExist()
        } else if (!connectionModel.isConnected()) {
            view.showPresharingImage(file.absolutePath)
            filePresharing = file
        } else {
            fileToSend = file
            if (connectionModel.isConnectedOrPending()) {
                sendFileIfPrepared()
            }
        }
    }

    fun cancelPresharing() {
        filePresharing = null
    }

    fun proceedPresharing() {

        filePresharing?.let {

            val currentConversation: Conversation? = connectionModel.getCurrentConversation()

            if (!connectionModel.isConnected()) {
                view.showPresharingImage(it.absolutePath)
            } else if (currentConversation != null && currentConversation.messageContractVersion < 1) {
                view.showReceiverUnableToReceiveImages()
            } else if (it.length() > maxFileSize) {
                view.showImageTooBig(maxFileSize.toLong())
            } else {
                connectionModel.sendFile(it, MessageType.IMAGE)
                filePresharing = null
            }
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

        connectionModel.getTransferringFile().let {

            if (it != null) {
                val type = if (it.transferType == TransferringFile.TransferType.RECEIVING)
                    ChatView.FileTransferType.RECEIVING else ChatView.FileTransferType.SENDING
                view.showImageTransferLayout(it.name, it.size, type)
            } else {
                view.hideImageTransferLayout()
            }
        }

        connectionModel.getCurrentConversation().let {

            if (it == null) {
                if (connectionModel.isPending()) {
                    view.showStatusPending()
                    view.showWainingForOpponent()
                } else {
                    view.showStatusNotConnected()
                    view.showNotConnectedToAnyDevice()
                }
            } else if (it.deviceAddress != deviceAddress) {
                view.showStatusNotConnected()
                view.showNotConnectedToThisDevice("${it.displayName} (${it.deviceName})")
            } else if (connectionModel.isPending() && it.deviceAddress == deviceAddress) {
                view.showStatusPending()
                view.showConnectionRequest(it.displayName, it.deviceName)
            } else {
                view.showStatusConnected()
            }
        }
    }
}
