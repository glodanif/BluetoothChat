package com.glodanif.bluetoothchat.ui.presenter

import android.bluetooth.BluetoothDevice
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.OnLifecycleEvent
import com.glodanif.bluetoothchat.data.entity.ChatMessage
import com.glodanif.bluetoothchat.data.entity.Conversation
import com.glodanif.bluetoothchat.data.model.*
import com.glodanif.bluetoothchat.data.service.message.Contract
import com.glodanif.bluetoothchat.data.service.message.PayloadType
import com.glodanif.bluetoothchat.data.service.message.TransferringFile
import com.glodanif.bluetoothchat.ui.view.ChatView
import com.glodanif.bluetoothchat.ui.viewmodel.converter.ChatMessageConverter
import kotlinx.coroutines.*
import java.io.File

class ChatPresenter(private val deviceAddress: String,
                    private val view: ChatView,
                    private val conversationsStorage: ConversationsStorage,
                    private val messagesStorage: MessagesStorage,
                    private val scanModel: BluetoothScanner,
                    private val connectionModel: BluetoothConnector,
                    private val preferences: UserPreferences,
                    private val converter: ChatMessageConverter,
                    private val uiContext: CoroutineDispatcher = Dispatchers.Main,
                    private val bgContext: CoroutineDispatcher = Dispatchers.IO) : BasePresenter(uiContext) {

    private val maxFileSize = 5_242_880

    private var fileToSend: File? = null
    private var filePresharing: File? = null

    private val prepareListener = object : OnPrepareListener {

        override fun onPrepared() {

            with(connectionModel) {
                addOnConnectListener(connectionListener)
                addOnMessageListener(messageListener)
                addOnFileListener(fileListener)
            }
            updateState()
            dismissNotification()

            if (!connectionModel.isConnected()) {
                fileToSend?.let {
                    filePresharing = fileToSend
                    view.showPresharingImage(it.absolutePath)
                }
            } else {

                if (filePresharing != null) {
                    return
                }

                fileToSend?.let { file ->

                    if (file.length() > maxFileSize) {
                        view.showImageTooBig(maxFileSize.toLong())
                    } else {
                        connectionModel.sendFile(file, PayloadType.IMAGE)
                    }
                    fileToSend = null
                    filePresharing = null
                }
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
                val conversation = withContext(bgContext) { conversationsStorage.getConversationByAddress(deviceAddress) }
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
                view.hideDisconnected()
                view.hideLostConnection()
                view.showConnectionRequest(conversation.displayName, conversation.deviceName)
                view.showPartnerName(conversation.displayName, conversation.deviceName)
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

        override fun onMessageSendingFailed() {
            view.showSendingMessageFailure()
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

    fun onViewCreated() = launch {
        val color = withContext(bgContext) { preferences.getChatBackgroundColor() }
        view.setBackgroundColor(color)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun prepareConnection() {

        if (!scanModel.isBluetoothEnabled()) {
            view.showBluetoothDisabled()
        } else {

            connectionModel.addOnPrepareListener(prepareListener)

            if (connectionModel.isConnectionPrepared()) {
                with(connectionModel) {
                    addOnConnectListener(connectionListener)
                    addOnMessageListener(messageListener)
                    addOnFileListener(fileListener)
                }
                updateState()
                dismissNotification()
                sendFileIfPrepared()
            } else {
                connectionModel.prepare()
            }
        }

        launch {
            val messagesDef = async(bgContext) { messagesStorage.getMessagesByDevice(deviceAddress) }
            val conversationDef = async(bgContext) { conversationsStorage.getConversationByAddress(deviceAddress) }
            displayInfo(messagesDef.await(), conversationDef.await())
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun releaseConnection() {
        with(connectionModel) {
            removeOnPrepareListener(prepareListener)
            removeOnConnectListener(connectionListener)
            removeOnMessageListener(messageListener)
            removeOnFileListener(fileListener)
        }
    }

    private fun sendFileIfPrepared() = fileToSend?.let { file ->

        if (connectionModel.isConnected()) {
            if (file.length() > maxFileSize) {
                view.showImageTooBig(maxFileSize.toLong())
            } else {
                connectionModel.sendFile(file, PayloadType.IMAGE)
            }
            fileToSend = null
            filePresharing = null
        } else {
            filePresharing = fileToSend
            view.showPresharingImage(file.absolutePath)
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

    fun resetConnection() {
        connectionModel.disconnect()
        view.showStatusNotConnected()
        view.showNotConnectedToAnyDevice()
    }

    fun disconnect() {
        connectionModel.sendDisconnectRequest()
        view.showStatusNotConnected()
        view.showNotConnectedToAnyDevice()
    }

    fun connectToDevice() {

        scanModel.getDeviceByAddress(deviceAddress).let { device ->

            if (device != null) {
                view.showStatusPending()
                view.showWainingForOpponent()
                connectionModel.connect(device)
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

    fun performFilePicking() {

        if (connectionModel.isFeatureAvailable(Contract.Feature.IMAGE_SHARING)) {
            view.openImagePicker()
        } else {
            view.showReceiverUnableToReceiveImages()
        }
    }

    fun sendFile(file: File) {

        if (!file.exists()) {
            view.showImageNotExist()
        } else if (!connectionModel.isConnectionPrepared()) {
            fileToSend = file
            connectionModel.addOnPrepareListener(prepareListener)
            connectionModel.prepare()
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
        fileToSend = null
        filePresharing = null
    }

    fun proceedPresharing() {

        filePresharing?.let {

            if (!connectionModel.isConnected()) {
                view.showPresharingImage(it.absolutePath)
            } else if (!connectionModel.isFeatureAvailable(Contract.Feature.IMAGE_SHARING)) {
                view.showReceiverUnableToReceiveImages()
            } else if (it.length() > maxFileSize) {
                view.showImageTooBig(maxFileSize.toLong())
            } else {
                connectionModel.sendFile(it, PayloadType.IMAGE)
                fileToSend = null
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

        connectionModel.getTransferringFile().let { file ->

            if (file != null) {
                val type = if (file.transferType == TransferringFile.TransferType.RECEIVING)
                    ChatView.FileTransferType.RECEIVING else ChatView.FileTransferType.SENDING
                view.showImageTransferLayout(file.name, file.size, type)
            } else {
                view.hideImageTransferLayout()
            }
        }

        connectionModel.getCurrentConversation().let { conversation ->

            if (conversation == null) {
                if (connectionModel.isPending()) {
                    view.showStatusPending()
                    view.showWainingForOpponent()
                } else {
                    view.showStatusNotConnected()
                    view.showNotConnectedToAnyDevice()
                }
            } else if (conversation.deviceAddress != deviceAddress) {
                view.showStatusNotConnected()
                view.showNotConnectedToThisDevice("${conversation.displayName} (${conversation.deviceName})")
            } else if (connectionModel.isPending() && conversation.deviceAddress == deviceAddress) {
                view.hideDisconnected()
                view.hideLostConnection()
                view.showStatusPending()
                view.showConnectionRequest(conversation.displayName, conversation.deviceName)
            } else {
                view.showStatusConnected()
            }
        }
    }
}
