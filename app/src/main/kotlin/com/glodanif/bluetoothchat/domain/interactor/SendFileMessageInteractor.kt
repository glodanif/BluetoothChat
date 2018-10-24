package com.glodanif.bluetoothchat.domain.interactor

import com.glodanif.bluetoothchat.data.model.BluetoothConnector
import com.glodanif.bluetoothchat.data.service.message.PayloadType
import com.glodanif.bluetoothchat.domain.exception.ConnectionException
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

            } else {
                connection.sendFile(input, PayloadType.IMAGE)
            }
            fileToSend = null
            filePresharing = null
        } else {
            filePresharing = fileToSend

        }
    }
}
