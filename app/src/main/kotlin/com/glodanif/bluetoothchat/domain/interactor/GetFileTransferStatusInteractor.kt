package com.glodanif.bluetoothchat.domain.interactor

import com.glodanif.bluetoothchat.data.model.BluetoothConnector
import com.glodanif.bluetoothchat.domain.FileTransferringStatus

class GetFileTransferStatusInteractor(private val connection: BluetoothConnector) : BaseInteractor<Unit, FileTransferringStatus>() {

    override suspend fun execute(input: Unit): FileTransferringStatus {

        val file = connection.getTransferringFile()

        return if (file != null) {
            FileTransferringStatus.Transferring(file.name, file.size, file.transferType)
        } else {
            FileTransferringStatus.NotTransferring
        }
    }
}
