package com.glodanif.bluetoothchat.domain.interactor

import com.glodanif.bluetoothchat.data.model.BluetoothConnector

class CancelFileTransferInteractor(private val connection: BluetoothConnector) : BaseInteractor<Unit, Unit>() {

    override suspend fun execute(input: Unit) {
        connection.cancelFileTransfer()
    }
}
