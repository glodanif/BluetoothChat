package com.glodanif.bluetoothchat.domain.interactor

import com.glodanif.bluetoothchat.data.model.BluetoothConnector
import com.glodanif.bluetoothchat.domain.exception.ConnectionException
import com.glodanif.bluetoothchat.domain.exception.InvalidStringException

class SendTextMessageInteractor(private val connection: BluetoothConnector) : BaseInteractor<String, Unit>() {

    override suspend fun execute(input: String) {

        if (connection.isConnected()) {
            if (input.isEmpty()) {
                throw InvalidStringException("Message is empty")
            } else {
                connection.sendMessage(input)
            }
        } else {
            throw ConnectionException("Not connected to any device")
        }
    }
}
