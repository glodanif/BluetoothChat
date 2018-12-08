package com.glodanif.bluetoothchat.domain.interactor

import com.glodanif.bluetoothchat.data.model.BluetoothConnector
import com.glodanif.bluetoothchat.data.service.message.TransferringFile
import com.glodanif.bluetoothchat.domain.ConversationStatus
import com.glodanif.bluetoothchat.domain.FileTransferringStatus

class GetFileTransferStatusInteractor(private val connection: BluetoothConnector) {

    fun execute(onResult: ((FileTransferringStatus) -> Unit)? = null) {

        val file = connection.getTransferringFile()
        if (file != null) {
            if (file.transferType == TransferringFile.TransferType.RECEIVING) {
                onResult?.invoke(FileTransferringStatus.Receiving(file.name, file.size))
            } else {
                onResult?.invoke(FileTransferringStatus.Sending(file.name, file.size))
            }
        } else {
            onResult?.invoke(FileTransferringStatus.NotTransferring)
        }
    }
}
