package com.glodanif.bluetoothchat.service

import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Binder
import android.os.IBinder
import android.support.v7.app.AppCompatActivity
import android.util.Log
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.*

class BluetoothConnectionService : Service() {

    private val binder = ConnectionBinder()

    private val TAG = "TAG13"

    private enum class ConnectionState { CONNECTED, CONNECTING, NOT_CONNECTED, LISTENING }

    private var listener: ConnectionServiceListener? = null

    private val adapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()

    private val APP_NAME = "BluetoothChat"
    private val APP_UUID = UUID.fromString("220da3b2-41f5-11e7-a919-92ebcb67fe33")

    private var acceptThread: AcceptThread? = null
    private var connectThread: ConnectThread? = null
    private var connectedThread: ConnectedThread? = null

    private var connectionState: ConnectionState = ConnectionState.NOT_CONNECTED

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

    @Synchronized fun connect(device: BluetoothDevice) {

        Log.d(TAG, "connect to: " + device)

        if (connectionState == ConnectionState.CONNECTING) {
            connectThread?.cancel()
            connectThread = null
        }

        connectedThread?.cancel()
        connectedThread = null

        connectThread = ConnectThread(device)
        connectThread!!.start()
        listener?.onConnecting()
    }

    @Synchronized fun connected(socket: BluetoothSocket, device: BluetoothDevice) {

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

    private fun connectionFailed() {
        listener?.onConnectionFailed()
        connectionState = ConnectionState.NOT_CONNECTED
        prepareForAccept()
    }

    private fun connectionLost() {
        listener?.onConnectionLost()
        connectionState = ConnectionState.NOT_CONNECTED
        prepareForAccept()
    }

    fun sendMessage(message: String) {

        if (connectionState == ConnectionState.CONNECTED) {
            connectedThread?.write(message.toByteArray(Charsets.UTF_16))
        }
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
                Log.e(TAG, "Socket listen() failed", e)
                e.printStackTrace()
            }

            connectionState = ConnectionState.LISTENING
        }

        override fun run() {

            Log.d(TAG, "BEGIN acceptThread" + this)

            var socket: BluetoothSocket?

            loop@ while (connectionState != ConnectionState.CONNECTED) {
                try {
                    socket = serverSocket?.accept()
                } catch (e: IOException) {
                    Log.e(TAG, "Socket accept() failed")
                    break
                }

                if (socket != null) {
                    when (connectionState) {
                        ConnectionState.LISTENING, ConnectionState.CONNECTING -> {
                            Log.e(TAG, "AcceptThread")
                            connected(socket, socket.remoteDevice)
                            break@loop
                        }
                        ConnectionState.NOT_CONNECTED, ConnectionState.CONNECTED -> try {
                            socket.close()
                        } catch (e: IOException) {
                            Log.e(TAG, "Could not close unwanted socket", e)
                        } finally {
                            break@loop
                        }
                    }
                }
            }

            Log.i(TAG, "END acceptThread")
        }

        fun cancel() {
            Log.d(TAG, "Socket cancel " + this)
            try {
                serverSocket?.close()
            } catch (e: IOException) {
                Log.e(TAG, "Socket close() of server failed", e)
                e.printStackTrace()
            }
        }
    }

    private inner class ConnectThread(bluetoothDevice: BluetoothDevice) : Thread() {

        private var socket: BluetoothSocket? = null
        private val device = bluetoothDevice

        init {
            try {
                socket = device.createRfcommSocketToServiceRecord(APP_UUID)
            } catch (e: IOException) {
                Log.e(TAG, "Socket create() failed", e)
                e.printStackTrace()
            }
            connectionState = ConnectionState.CONNECTING
        }

        override fun run() {

            Log.i(TAG, "BEGIN connectThread")

            try {
                socket?.connect()
            } catch (connectException: IOException) {
                connectException.printStackTrace()
                try {
                    socket?.close()
                } catch (closeException: IOException) {
                    closeException.printStackTrace()
                    Log.e(TAG, "unable to close() socket during connection failure", closeException)
                }
                connectionFailed()
                return
            }

            if (socket != null) {
                Log.e(TAG, "ConnectThread")
                connected(socket!!, device)
            }
        }

        fun cancel() {
            try {
                socket?.close()
            } catch (e: IOException) {
                e.printStackTrace()
                Log.e(TAG, "close() of connect socket failed", e)
            }

        }
    }

    private inner class ConnectedThread(private val socket: BluetoothSocket) : Thread() {

        private var inputStream: InputStream? = null
        private var outputStream: OutputStream? = null

        init {
            Log.d(TAG, "create ConnectedThread")

            try {
                inputStream = socket.inputStream
                outputStream = socket.outputStream
            } catch (e: IOException) {
                e.printStackTrace()
                Log.e(TAG, "sockets not created", e)
            }

            connectionState = ConnectionState.CONNECTED
        }

        override fun run() {
            Log.i(TAG, "BEGIN connectedThread")
            val buffer = ByteArray(1024)
            var bytes: Int?

            while (connectionState == ConnectionState.CONNECTED) {
                try {
                    bytes = inputStream?.read(buffer)
                    listener?.onMessageReceived("$bytes")
                } catch (e: IOException) {
                    Log.e(TAG, "disconnected", e)
                    connectionLost()
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
                socket.close()
            } catch (e: IOException) {
                Log.e(TAG, "close() of connect socket failed", e)
            }
        }
    }

    interface ConnectionServiceListener {
        fun onMessageReceived(message: String)
        fun onMessageSent(id: Int)
        fun onConnecting()
        fun onConnected(name: String)
        fun onConnectionLost()
        fun onConnectionFailed()
        fun onDisconnected()
    }

    companion object {

        fun bind(context: Context, connection: ServiceConnection) {
            val intent = Intent(context, BluetoothConnectionService::class.java)
            context.bindService(intent, connection, AppCompatActivity.BIND_AUTO_CREATE)
        }
    }
}
