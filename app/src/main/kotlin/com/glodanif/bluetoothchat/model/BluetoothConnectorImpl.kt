package com.glodanif.bluetoothchat.model

import android.bluetooth.BluetoothDevice
import android.content.ComponentName
import android.content.Context
import android.content.ServiceConnection
import android.os.IBinder
import com.glodanif.bluetoothchat.service.BluetoothConnectionService
import java.lang.IllegalStateException

class BluetoothConnectorImpl(private val context: Context) : BluetoothConnector {

    private var prepareListener: BluetoothConnector.OnPrepareListener? = null
    private var connectListener: BluetoothConnector.OnConnectListener? = null
    private var messageListener: BluetoothConnector.OnMessageListener? = null

    private var service: BluetoothConnectionService? = null
    private var bound = false

    private val connection = object : ServiceConnection {

        override fun onServiceConnected(className: ComponentName, binder: IBinder) {
            service = (binder as BluetoothConnectionService.ConnectionBinder).getService()
            bound = true
            prepareListener?.onPrepared()
        }

        override fun onServiceDisconnected(className: ComponentName) {
            service = null
            bound = false
            prepareListener?.onError()
        }
    }

    override fun prepare() {
        BluetoothConnectionService.bind(context, connection)
    }

    override fun release() {
        if (bound) {
            context.unbindService(connection)
            bound = false
        }
    }

    override fun setOnConnectListener(listener: BluetoothConnector.OnConnectListener) {
        this.connectListener = listener
    }

    override fun setOnPrepareListener(listener: BluetoothConnector.OnPrepareListener) {
        this.prepareListener = listener
    }

    override fun setOnMessageListener(listener: BluetoothConnector.OnMessageListener) {
        this.messageListener = listener
    }

    override fun prepareForAccept() {
        if (!bound) {
            throw IllegalStateException("Bluetooth connection service is not prepared yet")
        }

        service?.prepareForAccept()
    }

    override fun connect(device: BluetoothDevice) {
        if (!bound) {
            throw IllegalStateException("Bluetooth connection service is not prepared yet")
        }

        service?.connect(device)
    }

    override fun stop() {
        if (!bound) {
            throw IllegalStateException("Bluetooth connection service is not prepared yet")
        }

        service?.stop()
    }

    override fun sendMessage(message: String) {
        if (!bound) {
            throw IllegalStateException("Bluetooth connection service is not prepared yet")
        }

        service?.sendMessage(message)
    }
}