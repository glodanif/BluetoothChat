package com.glodanif.bluetoothchat.ui.presenter

import android.bluetooth.BluetoothDevice
import android.util.Log
import android.widget.ImageView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.glodanif.bluetoothchat.data.entity.ChatMessage
import com.glodanif.bluetoothchat.data.entity.Conversation
import com.glodanif.bluetoothchat.data.model.OnConnectionListener
import com.glodanif.bluetoothchat.data.model.OnFileListener
import com.glodanif.bluetoothchat.data.model.OnMessageListener
import com.glodanif.bluetoothchat.domain.ConversationStatus
import com.glodanif.bluetoothchat.domain.FileTransferringStatus
import com.glodanif.bluetoothchat.domain.exception.*
import com.glodanif.bluetoothchat.domain.interactor.*
import com.glodanif.bluetoothchat.ui.router.ChatRouter
import com.glodanif.bluetoothchat.ui.view.ChatView
import com.glodanif.bluetoothchat.ui.viewmodel.ChatMessageViewModel
import com.glodanif.bluetoothchat.ui.viewmodel.converter.ChatMessageConverter
import java.io.File

class ChatPresenter(private val deviceAddress: String,
                    private val view: ChatView,
                    private val router: ChatRouter,
                    private val getProfileInteractor: GetProfileInteractor,
                    private val markMessagesAsSeenInteractor: MarkMessagesAsSeenInteractor,
                    private val getConversationByAddressInteractor: GetConversationByAddressInteractor,
                    private val getMessagesByAddressInteractor: GetMessagesByAddressInteractor,
                    private val prepareBluetoothConnectionInteractor: PrepareBluetoothConnectionInteractor,
                    private val sendTextMessageInteractor: SendTextMessageInteractor,
                    private val sendFileMessageInteractor: SendFileMessageInteractor,
                    private val getConversationStatusInteractor: GetConversationStatusInteractor,
                    private val getFileTransferStatusInteractor: GetFileTransferStatusInteractor,
                    private val manageConnectionInteractor: ManageConnectionInteractor,
                    private val cancelFileTransferInteractor: CancelFileTransferInteractor,
                    private val converter: ChatMessageConverter
) : LifecycleObserver {

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
            loadPartnerInfo()
        }

        override fun onConnectionRejected() {
            view.showRejectedConnection()
            updateState()
        }

        override fun onConnectedIn(conversation: Conversation) {

            getConversationStatusInteractor.execute(deviceAddress,
                    onResult = { status ->
                        if (status is ConversationStatus.Connected) {
                            view.showStatusPending()
                            view.hideDisconnected()
                            view.hideLostConnection()
                            view.showConnectionRequest(conversation.displayName, conversation.deviceName)
                            view.showPartnerName(conversation.displayName, conversation.deviceName)
                        }
                    })
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

        getConversationStatusInteractor.execute(deviceAddress,
                onResult = { status ->
                    if (status is ConversationStatus.Connected || status is ConversationStatus.IncomingRequest) {
                        view.dismissMessageNotification()
                    }
                }
        )
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

        prepareBluetoothConnectionInteractor.prepare(connectionListener, messageListener, fileListener,
                onPrepared = {
                    updateState()
                    dismissNotification()
                },
                onError = { error ->
                    when (error) {
                        is BluetoothDisabledException -> view.showBluetoothDisabled()
                        is PreparationException -> prepareBluetoothConnectionInteractor.release()
                    }
                }
        )

        getMessagesByAddressInteractor.execute(deviceAddress,
                onResult = { messages ->
                    displayMessages(messages)
                }
        )

        loadPartnerInfo()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun releaseConnection() {
        getProfileInteractor.cancel()
        markMessagesAsSeenInteractor.cancel()
        getConversationByAddressInteractor.cancel()
        getMessagesByAddressInteractor.cancel()
        sendTextMessageInteractor.cancel()
        sendFileMessageInteractor.cancel()
        manageConnectionInteractor.cancel()
        cancelFileTransferInteractor.cancel()
    }

    fun onImageClick(view: ImageView, message: ChatMessageViewModel) {
        router.openImage(view, message)
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

        markMessagesAsSeenInteractor.execute(messages,
                onResult = { updatedMessages ->
                    view.showMessagesHistory(converter.transform(updatedMessages))
                })
    }

    fun resetConnection() {
        manageConnectionInteractor.disconnect()
        view.showStatusNotConnected()
        view.showNotConnectedToAnyDevice()
    }

    fun disconnect() {
        manageConnectionInteractor.sendDisconnectionRequest()
        view.showStatusNotConnected()
        view.showNotConnectedToAnyDevice()
    }

    fun connectToDevice() {

        manageConnectionInteractor.connect(deviceAddress,
                onResult = {
                    view.showStatusPending()
                    view.showWainingForOpponent()
                },
                onError = { error ->
                    when (error) {
                        is BluetoothDisabledException -> view.showBluetoothDisabled()
                        is DeviceNotFoundException -> {
                            view.showStatusNotConnected()
                            view.showDeviceIsNotAvailable()
                        }
                    }
                }
        )
    }

    fun sendMessage(message: String) {

        sendTextMessageInteractor.execute(message,
                onResult = {
                    view.afterMessageSent()
                },
                onError = { error ->
                    when (error) {
                        is InvalidStringException -> view.showNotValidMessage()
                        is ConnectionException -> view.showNotConnectedToSend()
                    }
                }
        )
    }

    fun performFilePicking() {

        sendFileMessageInteractor.prepareFilePicking(
                onResult = {
                    view.openImagePicker()
                },
                onError = {
                    view.showReceiverUnableToReceiveImages()
                }
        )
    }

    fun sendFile(file: File) {
        sendFileMessageInteractor.execute(file, onError = {
            Log.e("TAG13", it.message)
        })
    }

    fun cancelPresharing() {
        sendFileMessageInteractor.cancelPresharing()
    }

    fun proceedPresharing() {
        sendFileMessageInteractor.proceedPresharing()
    }

    fun cancelFileTransfer() {
        cancelFileTransferInteractor.execute(Unit)
        view.hideImageTransferLayout()
    }

    fun acceptConnection() {
        view.hideActions()
        view.showStatusConnected()
        manageConnectionInteractor.acceptConnection()
    }

    fun rejectConnection() {
        view.hideActions()
        view.showStatusNotConnected()
        manageConnectionInteractor.rejectConnection()
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

        getFileTransferStatusInteractor.execute(
                onResult = { status ->
                    when (status) {
                        is FileTransferringStatus.NotTransferring ->
                            view.hideImageTransferLayout()
                        is FileTransferringStatus.Sending -> {
                            view.showImageTransferLayout(status.fileName, status.fileSize, ChatView.FileTransferType.SENDING)
                        }
                        is FileTransferringStatus.Receiving -> {
                            view.showImageTransferLayout(status.fileName, status.fileSize, ChatView.FileTransferType.RECEIVING)
                        }
                    }
                }
        )

        getConversationStatusInteractor.execute(deviceAddress,
                onResult = { status ->
                    when (status) {
                        is ConversationStatus.Connected ->
                            view.showStatusConnected()
                        is ConversationStatus.Pending -> {
                            view.showStatusPending()
                            view.showWainingForOpponent()
                        }
                        is ConversationStatus.NotConnected -> {
                            view.showStatusNotConnected()
                            view.showNotConnectedToAnyDevice()
                        }
                        is ConversationStatus.ConnectedToOther -> {
                            view.showStatusNotConnected()
                            view.showNotConnectedToThisDevice(status.displayName, status.deviceName)
                        }
                        is ConversationStatus.IncomingRequest -> {
                            view.hideDisconnected()
                            view.hideLostConnection()
                            view.showStatusPending()
                            view.showConnectionRequest(status.displayName, status.deviceName)
                        }
                    }
                })
    }
}
