package com.glodanif.bluetoothchat.domain.interactor

import com.glodanif.bluetoothchat.data.model.BluetoothConnector
import com.glodanif.bluetoothchat.domain.exception.ConnectionException
import com.glodanif.bluetoothchat.domain.exception.InvalidStringException

class DisconnectInteractor(private val connection: BluetoothConnector) : BaseInteractor<Unit, Unit>() {

    override suspend fun execute(input: Unit) {
        connection.disconnect()
    }
}
