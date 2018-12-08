package com.glodanif.bluetoothchat.domain.interactor

import com.glodanif.bluetoothchat.data.model.BluetoothConnector
import com.glodanif.bluetoothchat.data.service.message.Contract
import com.glodanif.bluetoothchat.data.service.message.PayloadType
import com.glodanif.bluetoothchat.domain.exception.ConnectionException
import com.glodanif.bluetoothchat.domain.exception.FeatureIsNotAvailableException
import com.glodanif.bluetoothchat.domain.exception.FileException
import java.io.File

class SendFileMessageInteractor(private val connection: BluetoothConnector) : BaseInteractor<File, Unit>() {

    private val maxFileSize = 5_242_880

    private var fileToSend: File? = null
    private var filePresharing: File? = null

    override suspend fun execute(input: File) {

        if (!input.exists()) {
            throw FileException("File doesn't exist")
        } else if (!connection.isConnectionPrepared()) {
            fileToSend = input
            throw ConnectionException("Connection is not prepared")
        } else if (!connection.isConnected()) {
            filePresharing = input
            throw ConnectionException("Not connected to any device")
        } else {
            fileToSend = input
            if (connection.isConnectedOrPending()) {
                sendFileIfPrepared(input)
            }
        }
    }

    private fun sendFileIfPrepared(input: File) {
        if (connection.isConnected()) {
            if (input.length() > maxFileSize) {
                throw FileException("File is too big")
            } else {
                connection.sendFile(input, PayloadType.IMAGE)
            }
            fileToSend = null
            filePresharing = null
        } else {
            filePresharing = fileToSend

        }
    }

    fun prepareFilePicking(onResult: (() -> Unit)? = null, onError: ((Throwable) -> Unit)? = null) {
        if (!connection.isFeatureAvailable(Contract.Feature.IMAGE_SHARING)) {
            onError?.invoke(FeatureIsNotAvailableException())
        } else {
            onResult?.invoke()
        }
    }

    fun cancelPresharing() {
        fileToSend = null
        filePresharing = null
    }

    fun proceedPresharing() {

        filePresharing?.let {

            if (!connection.isConnected()) {
                throw ConnectionException("Not connected to any device")
            } else if (!connection.isFeatureAvailable(Contract.Feature.IMAGE_SHARING)) {
                throw FeatureIsNotAvailableException()
            } else if (it.length() > maxFileSize) {
                throw FileException("File is too big")
            } else {
                connection.sendFile(it, PayloadType.IMAGE)
                fileToSend = null
                filePresharing = null
            }
        }
    }
}
