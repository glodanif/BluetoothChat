package com.glodanif.bluetoothchat.domain.interactor

import com.glodanif.bluetoothchat.data.model.BluetoothScanner
import com.glodanif.bluetoothchat.domain.exception.BluetoothStatusException

class DiscoverableInteractor(private val scanner: BluetoothScanner) {

    fun setListeners(
            onStart: (() -> Unit)? = null,
            onFinish: (() -> Unit)? = null
    ) {

        scanner.setDiscoverableListener(object : BluetoothScanner.DiscoverableListener {

            override fun onDiscoverableStart() {
                onStart?.invoke()
            }

            override fun onDiscoverableFinish() {
                onFinish?.invoke()
            }
        })
    }

    fun listen(
            onResult: (() -> Unit)? = null,
            onError: ((Throwable) -> Unit)? = null
    ) {
        if (scanner.isDiscoverable()) {
            scanner.listenDiscoverableStatus()
            onResult?.invoke()
        } else {
            onError?.invoke(BluetoothStatusException("Device is already discoverable"))
        }
    }

    fun checkDiscoverability(
            onResult: (() -> Unit)? = null,
            onError: ((Throwable) -> Unit)? = null
    ) {
        if (scanner.isDiscoverable()) {
            onResult?.invoke()
        } else {
            onError?.invoke(BluetoothStatusException("Device is already discoverable"))
        }
    }

    fun stopListening() {
        scanner.stopListeningDiscoverableStatus()
    }
}
