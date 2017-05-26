package com.glodanif.bluetoothchat.model

import android.bluetooth.BluetoothDevice
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.support.v7.app.AppCompatActivity
import com.glodanif.bluetoothchat.service.BluetoothConnectionService
import java.lang.IllegalStateException

class BluetoothConnectorImpl(private val context: Context, private val device: BluetoothDevice) : BluetoothConnector {

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
        val intent = Intent(context, BluetoothConnectionService::class.java)
        context.bindService(intent, connection, AppCompatActivity.BIND_AUTO_CREATE)
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

    override fun connect() {
        if (!bound) {
            throw IllegalStateException("Bluetooth connection service is not prepared yet")
        }

        service?.connect()
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