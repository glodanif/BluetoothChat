package com.glodanif.bluetoothchat.domain.interactor

import com.glodanif.bluetoothchat.data.model.BluetoothConnector

class IsConnectedToThisDeviceInteractor(private val connection: BluetoothConnector) : BaseInteractor<String, Boolean>() {

    override suspend fun execute(input: String) =
            connection.getCurrentConversation()?.deviceAddress == input
}
