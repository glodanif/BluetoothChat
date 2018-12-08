package com.glodanif.bluetoothchat.domain.interactor

import com.glodanif.bluetoothchat.data.model.BluetoothConnector
import com.glodanif.bluetoothchat.data.model.BluetoothScanner
import com.glodanif.bluetoothchat.domain.exception.BluetoothDisabledException
import com.glodanif.bluetoothchat.domain.exception.DeviceNotFoundException

class ManageConnectionInteractor(private val connection: BluetoothConnector, private val scanner: BluetoothScanner) : BaseInteractor<Unit, Unit>() {

    override suspend fun execute(input: Unit) {}

    fun connect(address: String, onResult: (() -> Unit)? = null, onError: ((Throwable) -> Unit)? = null) {

        val device = scanner.getDeviceByAddress(address)
        if (!scanner.isBluetoothEnabled()) {
            onError?.invoke(BluetoothDisabledException())
        } else if (device == null) {
            onError?.invoke(DeviceNotFoundException())
        } else {
            connection.connect(device)
            onResult?.invoke()
        }
    }

    fun disconnect() {
        connection.disconnect()
    }

    fun sendDisconnectionRequest() {
        connection.sendDisconnectRequest()
    }

    fun acceptConnection() {
        connection.acceptConnection()
    }

    fun rejectConnection() {
        connection.rejectConnection()
    }
}
