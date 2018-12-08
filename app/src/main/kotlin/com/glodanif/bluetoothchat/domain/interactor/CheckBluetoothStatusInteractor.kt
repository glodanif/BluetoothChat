package com.glodanif.bluetoothchat.domain.interactor

import com.glodanif.bluetoothchat.data.model.BluetoothScanner
import com.glodanif.bluetoothchat.domain.exception.BluetoothStatusException

class CheckBluetoothStatusInteractor(private val scanner: BluetoothScanner) {

    fun checkIfAvailable(
            onResult: (() -> Unit)? = null,
            onError: ((Throwable) -> Unit)? = null
    ) {
        if (scanner.isBluetoothAvailable()) {
            onResult?.invoke()
        } else {
            onError?.invoke(BluetoothStatusException("Bluetooth is not available"))
        }
    }

    fun checkIfEnabled(
            onResult: (() -> Unit)? = null,
            onError: ((Throwable) -> Unit)? = null
    ) {
        if (scanner.isBluetoothEnabled()) {
            onResult?.invoke()
        } else {
            onError?.invoke(BluetoothStatusException("Bluetooth is disabled"))
        }
    }
}
