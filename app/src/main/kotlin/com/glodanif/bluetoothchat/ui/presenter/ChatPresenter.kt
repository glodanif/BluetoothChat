package com.glodanif.bluetoothchat.ui.presenter

import android.bluetooth.BluetoothDevice
import android.widget.ImageView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.glodanif.bluetoothchat.data.entity.ChatMessage
import com.glodanif.bluetoothchat.data.entity.Conversation
import com.glodanif.bluetoothchat.data.model.*
import com.glodanif.bluetoothchat.data.service.message.Contract
import com.glodanif.bluetoothchat.data.service.message.PayloadType
import com.glodanif.bluetoothchat.data.service.message.TransferringFile
import com.glodanif.bluetoothchat.domain.interactor.*
import com.glodanif.bluetoothchat.ui.router.ChatRouter
import com.glodanif.bluetoothchat.ui.view.ChatView
import com.glodanif.bluetoothchat.ui.viewmodel.ChatMessageViewModel
import com.glodanif.bluetoothchat.ui.viewmodel.converter.ChatMessageConverter
import java.io.File

class ChatPresenter(private val deviceAddress: String,
                    private val view: ChatView,
                    private val router: ChatRouter,
                    private val scanModel: BluetoothScanner,
                    private val connectionModel: BluetoothConnector,
                    private val getProfileInteractor: GetProfileInteractor,
                    private val markMessagesAsSeenMessagesInteractor: MarkMessagesAsSeenMessagesInteractor,
                    private val getConversationByAddressInteractor: GetConversationByAddressInteractor,
                    private val getMessagesByAddressInteractor: GetMessagesByAddressInteractor,
                    private val subscribeForConnectionEventsInteractor: SubscribeForConnectionEventsInteractor,

                    private val converter: ChatMessageConverter
) : LifecycleObserver {

    private val maxFileSize = 5_242_880

    private var fileToSend: File? = null
    private var filePresharing: File? = null

    private val prepareListener = object : OnPrepareListener {

        override fun onPrepared() {

            subscribeForConnectionEvents()

            with(connectionModel) {
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

    private fun subscribeForConnectionEvents() {

        subscribeForConnectionEventsInteractor.subscribe(

                onConnectionWithdrawn = {
                    updateState()
                },

                onConnectionDestroyed = {
                    view.showServiceDestroyed()
                },

                onConnectionAccepted = {
                    view.showStatusConnected()
                    view.hideActions()
                    loadPartnerInfo()
                },

                onConnectionRejected = {
                    view.showRejectedConnection()
                    updateState()
                },

                onConnectedIn = { conversation ->
                    val currentConversation: Conversation? = connectionModel.getCurrentConversation()
                    if (currentConversation?.deviceAddress == deviceAddress) {
                        view.showStatusPending()
                        view.hideDisconnected()
                        view.hideLostConnection()
                        view.showConnectionRequest(conversation.displayName, conversation.deviceName)
                        view.showPartnerName(conversation.displayName, conversation.deviceName)
                    }
                },

                onConnectionLost = {
                    view.showLostConnection()
                    updateState()
                },

                onConnectionFailed = {
                    view.showFailedConnection()
                    updateState()
                },

                onDisconnected = {
                    view.showDisconnected()
                    updateState()
                }
        )
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

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun created() {

        getProfileInteractor.execute(Unit,
                onResult = { profile ->
                    view.setBackgroundColor(profile.color)
                }
        )
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun prepareConnection() {

        if (!scanModel.isBluetoothEnabled()) {
            view.showBluetoothDisabled()
        } else {

            connectionModel.addOnPrepareListener(prepareListener)

            if (connectionModel.isConnectionPrepared()) {
                subscribeForConnectionEvents()
                with(connectionModel) {
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

        loadPartnerInfo()
        getMessagesByAddressInteractor.execute(deviceAddress,
                onResult = { messages ->
                    displayMessages(messages)
                })
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun releaseConnection() {
        subscribeForConnectionEventsInteractor.unsubscribe()
        with(connectionModel) {
            removeOnPrepareListener(prepareListener)
            removeOnMessageListener(messageListener)
            removeOnFileListener(fileListener)
        }

        getProfileInteractor.cancel()
        markMessagesAsSeenMessagesInteractor.cancel()
        getConversationByAddressInteractor.cancel()
        getMessagesByAddressInteractor.cancel()
    }

    fun onImageClick(view: ImageView, message: ChatMessageViewModel) {
        router.openImage(view, message)
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

    private fun loadPartnerInfo() {

        getConversationByAddressInteractor.execute(deviceAddress,
                onResult = { conversation ->
                    if (conversation != null) {
                        view.showPartnerName(conversation.displayName, conversation.deviceName)
                    }
                }
        )
    }

    private fun displayMessages(messages: List<ChatMessage>) {

        markMessagesAsSeenMessagesInteractor.execute(messages,
                onResult = { updatedMessages ->
                    view.showMessagesHistory(converter.transform(updatedMessages))
                })
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
