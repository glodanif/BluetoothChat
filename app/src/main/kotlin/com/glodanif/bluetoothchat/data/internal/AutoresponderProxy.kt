package com.glodanif.bluetoothchat.data.internal

import android.bluetooth.BluetoothDevice
import android.os.Handler
import com.glodanif.bluetoothchat.data.entity.*
import com.glodanif.bluetoothchat.data.model.ProfileManagerImpl
import com.glodanif.bluetoothchat.data.service.BluetoothConnectionService
import com.glodanif.bluetoothchat.data.service.message.Contract
import com.glodanif.bluetoothchat.data.service.message.PayloadType
import java.io.File

class AutoresponderProxy(private val service: BluetoothConnectionService?) : CommunicationProxy {

    companion object {

        const val COMMAND_SEND_TEXT = "-send_text"
        const val COMMAND_SEND_FILE = "-send_file"
        const val COMMAND_SEND_FILE_AND_CANCEL = "-send_file_and_cancel"
        const val COMMAND_DISCONNECT = "-disconnect"

        const val RESPONSE_RECEIVED = "+text_message"
    }

    private val contract = Contract()
    private val handler = Handler()
    private val file = File("/storage/emulated/0/BLC DEV/image.jpg")

    private fun runWithDelay(delay: Long = 500, task: () -> Unit) {
        handler.postDelayed(task, delay)
    }

    override fun onConnecting() {
    }

    override fun onConnected(device: BluetoothDevice) {
    }

    override fun onConnectedIn(conversation: Conversation) {

        runWithDelay {
            service?.let {
                val profileManager = ProfileManagerImpl(it)
                val message = contract.createAcceptConnectionMessage(profileManager.getUserName(), profileManager.getUserColor())
                it.sendMessage(message)
            }
        }
    }

    override fun onConnectedOut(conversation: Conversation) {
    }

    override fun onConnectionLost() {
    }

    override fun onConnectionFailed() {
    }

    override fun onConnectionDestroyed() {
    }

    override fun onDisconnected() {
    }

    override fun onConnectionAccepted() {
    }

    override fun onConnectionRejected() {
    }

    override fun onConnectionWithdrawn() {
    }

    override fun onMessageReceived(message: ChatMessage) {

        runWithDelay {

            service?.let {

                when {
                    message.text == COMMAND_SEND_TEXT -> {
                        val chatMessage = contract.createChatMessage(RESPONSE_RECEIVED)
                        it.sendMessage(chatMessage)
                    }
                    message.text == COMMAND_SEND_FILE -> {
                        it.sendFile(file, PayloadType.IMAGE)
                    }
                    message.text == COMMAND_SEND_FILE_AND_CANCEL -> {
                        it.sendFile(file, PayloadType.IMAGE)
                        runWithDelay(2000) {
                            it.cancelFileTransfer()
                        }
                    }
                    message.text == COMMAND_DISCONNECT -> {
                        val chatMessage = contract.createDisconnectMessage()
                        it.sendMessage(chatMessage)
                    }
                }
            }
        }
    }

    override fun onMessageSent(message: ChatMessage) {
    }

    override fun onMessageSendingFailed() {
    }

    override fun onMessageDelivered(id: Long) {
    }

    override fun onMessageNotDelivered(id: Long) {
    }

    override fun onMessageSeen(id: Long) {
    }

    override fun onFileSendingStarted(fileAddress: String?, fileSize: Long) {
    }

    override fun onFileSendingProgress(sentBytes: Long, totalBytes: Long) {
    }

    override fun onFileSendingFinished() {
    }

    override fun onFileSendingFailed() {
    }

    override fun onFileReceivingStarted(fileSize: Long) {
    }

    override fun onFileReceivingProgress(sentBytes: Long, totalBytes: Long) {
    }

    override fun onFileReceivingFinished() {
    }

    override fun onFileReceivingFailed() {
    }

    override fun onFileTransferCanceled(byPartner: Boolean) {
    }
}
