package com.glodanif.bluetoothchat.domain.interactor

import android.bluetooth.BluetoothDevice
import com.glodanif.bluetoothchat.data.model.*
import com.glodanif.bluetoothchat.domain.exception.ConnectionException
import com.glodanif.bluetoothchat.domain.exception.DeviceNotFoundException
import com.glodanif.bluetoothchat.domain.exception.PreparationException

class ConnectToDeviceInteractor(private val connection: BluetoothConnector, private val scanner: BluetoothScanner) {

    private var onResult: ((BluetoothDevice) -> Unit)? = null
    private var onError: ((Throwable) -> Unit)? = null

    private var connectionListener = object : SimpleConnectionListener() {

        override fun onConnected(device: BluetoothDevice) {
            connection.removeOnConnectListener(this)
            onResult?.invoke(device)
        }

        override fun onConnectionLost() {
            connection.removeOnConnectListener(this)
            onError?.invoke(ConnectionException("Connection lost"))
        }

        override fun onConnectionFailed() {
            connection.removeOnConnectListener(this)
            onError?.invoke(ConnectionException("Connection failed"))
        }
    }

    fun connect(address: String, onResult: ((BluetoothDevice) -> Unit)? = null, onError: ((Throwable) -> Unit)? = null) {

        this.onResult = onResult
        this.onError = onError

        val device = scanner.getDeviceByAddress(address)
        if (device == null) {
            onError?.invoke(DeviceNotFoundException())
            return
        }

        if (connection.isConnectionPrepared()) {
            connection.addOnConnectListener(connectionListener)
            connection.connect(device)
            return
        }

        connection.addOnPrepareListener(object : OnPrepareListener {

            override fun onPrepared() {
                connection.removeOnPrepareListener(this)
                connection.connect(device)
            }

            override fun onError() {
                connection.removeOnPrepareListener(this)
                onError?.invoke(PreparationException())
            }
        })

        connection.prepare()
    }

    fun release() {
        connection.removeOnConnectListener(connectionListener)
    }
}
