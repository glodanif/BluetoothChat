package com.glodanif.bluetoothchat.domain.interactor

import com.glodanif.bluetoothchat.data.model.*
import com.glodanif.bluetoothchat.domain.exception.BluetoothDisabledException
import com.glodanif.bluetoothchat.domain.exception.PreparationException

class PrepareBluetoothConnectionInteractor(private val connection: BluetoothConnector, private val scanner: BluetoothScanner) : BaseInteractor<Unit, Unit>() {

    override suspend fun execute(input: Unit) {}

    private var prepareListener: OnPrepareListener? = null
    private var connectionListener: OnConnectionListener? = null
    private var messageListener: OnMessageListener? = null
    private var fileListener: OnFileListener? = null

    fun prepare(connectionListener: OnConnectionListener, messageListener: OnMessageListener, fileListener: OnFileListener,
                onPrepared: (() -> Unit)? = null, onError: ((Throwable) -> Unit)? = null) {

        this.connectionListener = connectionListener
        this.messageListener = messageListener
        this.fileListener = fileListener
        this.prepareListener = object : OnPrepareListener {

            override fun onPrepared() {

                connection.addOnConnectListener(connectionListener)
                connection.addOnMessageListener(messageListener)
                connection.addOnFileListener(fileListener)

                onPrepared?.invoke()
            }

            override fun onError() {
                onError?.invoke(PreparationException("Unable to prepare connection"))
            }
        }

        if (!scanner.isBluetoothEnabled()) {
            onError?.invoke(BluetoothDisabledException())
            return
        }

        if (connection.isConnectionPrepared()) {

            connection.addOnConnectListener(connectionListener)
            connection.addOnMessageListener(messageListener)
            connection.addOnFileListener(fileListener)

            onPrepared?.invoke()

        } else {
            prepareListener?.let {
                connection.addOnPrepareListener(it)
            }
            connection.prepare()
        }
    }

    fun release() {
        prepareListener?.let {
            connection.removeOnPrepareListener(it)
            prepareListener = null
        }
        connectionListener?.let {
            connection.removeOnConnectListener(it)
            connectionListener = null
        }
        messageListener?.let {
            connection.removeOnMessageListener(it)
            messageListener = null
        }
        fileListener?.let {
            connection.removeOnFileListener(it)
            fileListener = null
        }
    }
}
