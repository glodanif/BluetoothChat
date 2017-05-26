package com.glodanif.bluetoothchat.service

import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.*

class BluetoothConnectionService : Service() {

    private val binder = ConnectionBinder()

    private val TAG = "TAG13"

    private enum class ConnectionState { CONNECTED, CONNECTING, NOT_CONNECTED }

    private var listener: ConnectionServiceListener? = null

    private val adapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()

    private val APP_NAME = "BluetoothChat"
    private val APP_UUID = UUID.fromString("220da3b2-41f5-11e7-a919-92ebcb67fe33")

    private var acceptThread: AcceptThread? = null
    private var connectThread: ConnectThread? = null
    private var connectedThread: ConnectedThread? = null

    private var connectionState: ConnectionState = ConnectionState.NOT_CONNECTED

    private var device: BluetoothDevice? = null

    override fun onBind(intent: Intent): IBinder? {
        return binder
    }

    inner class ConnectionBinder : Binder() {

        fun getService(): BluetoothConnectionService {
            return this@BluetoothConnectionService
        }
    }

    @Synchronized fun prepareForAccept() {

        Log.d(TAG, "start")

        connectThread?.cancel()
        connectThread = null
        connectedThread?.cancel()
        connectedThread = null

        if (acceptThread == null) {
            acceptThread = AcceptThread()
            acceptThread!!.start()
        }
    }

    @Synchronized fun connect() {

        Log.d(TAG, "connect to: " + device)

        connectThread?.cancel()
        connectThread = null
        connectedThread?.cancel()
        connectedThread = null

        if (device != null) {
            connectThread = ConnectThread(device!!)
            connectThread!!.start()
            listener?.onConnecting()
        } else {
            listener?.onDisconnected()
        }
    }

    @Synchronized fun connected(socket: BluetoothSocket?, device: BluetoothDevice) {

        Log.d(TAG, "connected")

        connectThread?.cancel()
        connectThread = null

        connectedThread?.cancel()
        connectedThread = null

        acceptThread?.cancel()
        acceptThread = null

        connectedThread = ConnectedThread(socket)
        connectedThread!!.start()

        listener?.onConnected(device.name)
    }

    @Synchronized fun stop() {

        Log.d(TAG, "stop")

        connectThread?.cancel()
        connectThread = null

        connectedThread?.cancel()
        connectedThread = null

        acceptThread?.cancel()
        acceptThread = null

        connectionState = ConnectionState.NOT_CONNECTED
        listener?.onDisconnected()
    }

    fun write(out: ByteArray) {
        // Create temporary object
        var r: ConnectedThread? = null
        // Synchronize a copy of the ConnectedThread
        synchronized(this) {
            if (connectionState != ConnectionState.CONNECTED) return
            r = connectedThread
        }
        // Perform the write unsynchronized
        r?.write(out)
    }

    fun sendMessage(message: String) {
        var r: ConnectedThread? = null
        // Synchronize a copy of the ConnectedThread
        synchronized(this) {
            if (connectionState != ConnectionState.CONNECTED) return
            r = connectedThread
        }
        // Perform the write unsynchronized
        r?.write(message.toByteArray(Charsets.UTF_8))
    }

    fun setConnectionListener(listener: ConnectionServiceListener) {
        this.listener = listener
    }

    private inner class AcceptThread : Thread() {

        private var serverSocket: BluetoothServerSocket? = null

        init {
            try {
                serverSocket = adapter?.listenUsingRfcommWithServiceRecord(APP_NAME, APP_UUID)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        override fun run() {

            var socket: BluetoothSocket?

            while (true) {
                try {
                    socket = serverSocket?.accept()
                } catch (e: IOException) {
                    break
                }

                if (socket != null) {
                    manageConnectedSocket(socket)
                    serverSocket?.close()
                    break
                }
            }
        }

        fun cancel() {
            try {
                serverSocket?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private inner class ConnectThread(device: BluetoothDevice) : Thread() {

        private var socket: BluetoothSocket? = null

        init {
            try {
                socket = device.createRfcommSocketToServiceRecord(APP_UUID)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        override fun run() {
            try {
                socket?.connect()
            } catch (connectException: IOException) {
                connectException.printStackTrace()
                try {
                    socket?.close()
                } catch (closeException: IOException) {
                    closeException.printStackTrace()
                }
                return
            }

            if (device != null) {
                connected(socket, device!!)
            }
        }

        fun cancel() {
            try {
                socket?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }

        }
    }

    private inner class ConnectedThread(private val socket: BluetoothSocket?) : Thread() {

        private var inputStream: InputStream? = null
        private var outputStream: OutputStream? = null

        init {
            Log.d(TAG, "create ConnectedThread")

            try {
                inputStream = socket?.inputStream
                outputStream = socket?.outputStream
            } catch (e: IOException) {
                Log.e(TAG, "temp sockets not created", e)
            }
        }

        override fun run() {
            Log.i(TAG, "BEGIN mConnectedThread")
            val buffer = ByteArray(1024)
            var bytes: Int?

            while (connectionState == ConnectionState.CONNECTED) {
                try {
                    bytes = inputStream?.read(buffer)
                    listener?.onMessageReceived("$bytes")
                } catch (e: IOException) {
                    Log.e(TAG, "disconnected", e)
                    listener?.onConnectionLost()
                    break
                }
            }
        }

        fun write(buffer: ByteArray) {
            try {
                outputStream?.write(buffer)
                listener?.onMessageSent(-1)
            } catch (e: IOException) {
                Log.e(TAG, "Exception during write", e)
            }

        }

        fun cancel() {
            try {
                socket?.close()
            } catch (e: IOException) {
                Log.e(TAG, "close() of connect socket failed", e)
            }
        }
    }

    private fun manageConnectedSocket(socket: BluetoothSocket?) {
        TODO("not implemented")
    }

    interface ConnectionServiceListener {
        fun onMessageReceived(message: String)
        fun onMessageSent(id: Int)
        fun onConnecting()
        fun onConnected(name: String)
        fun onConnectionLost()
        fun onDisconnected()
    }
}