package com.glodanif.bluetoothchat.data.service

import android.bluetooth.BluetoothSocket
import android.content.Context
import android.support.constraint.solver.widgets.ConstraintAnchor
import com.glodanif.bluetoothchat.data.model.FilesManager
import com.glodanif.bluetoothchat.data.model.FilesManagerImpl
import java.io.*

abstract class DataTransferThread(context: Context, private val socket: BluetoothSocket, private val type: BluetoothConnectionService.ConnectionType, private val listener: TransferEventsListener) : Thread() {

    private val filesManager: FilesManager = FilesManagerImpl(context)

    private var inputStream: InputStream? = null
    private var outputStream: OutputStream? = null

    private var skipEvents = false

    private val buffer = ByteArray(1024)
    private var bytes: Int? = null

    private var isConnectionPrepared = false

    @Volatile
    var isFileLoading = false
    @Volatile
    var fileName: String? = null
    var fileSize: Long = 0

    fun prepare() {

        try {
            inputStream = socket.inputStream
            outputStream = socket.outputStream
            isConnectionPrepared = true
            listener.onConnectionPrepared(type)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    abstract fun shouldRun(): Boolean

    override fun start() {

        if (!isConnectionPrepared) {
            throw IllegalStateException("Connection is not prepared yet.")
        }

        super.start()
    }

    override fun run() {

        while (shouldRun()) {
            try {

                val message = readString()

                if (message != null && message.contains("6#0#0#")) {

                    isFileLoading = true
                    fileName = message.replace("6#0#0#", "").substringBefore("#")
                    fileSize = message.replace("6#0#0#", "").substringAfter("#").substringBefore("#").toLong()

                    listener.onMessageReceived(message)

                    filesManager.saveFile(inputStream!!, fileName!!, fileSize)

                } else {
                    if (message != null && message.contains("#")) {
                        listener.onMessageReceived(message)
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
                if (!skipEvents) {
                    listener.onConnectionLost()
                    skipEvents = false
                }
                break
            }
        }
    }

    private fun readString(): String? {
        bytes = inputStream?.read(buffer)
        return if (bytes != null) String(buffer, 0, bytes!!) else null
    }

    fun write(message: String) {
        write(message, false)
    }

    fun write(message: String, skipEvents: Boolean) {

        this.skipEvents = skipEvents

        try {
            outputStream?.write(message.toByteArray(Charsets.UTF_8))
            outputStream?.flush()
            listener.onMessageSent(message)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun writeFile(file: File) {

        val fileStream = FileInputStream(file)
        val bis = BufferedInputStream(fileStream)
        val bos = BufferedOutputStream(outputStream)

        try {

            var sentBytes: Long = 0
            var length: Int
            val buffer = ByteArray(1024)

            length = bis.read(buffer)
            while (length > -1) {
                if (length > 0) {
                    bos.write(buffer, 0, length)
                    bos.flush()
                    sentBytes += length.toLong()
                }
                length = bis.read(buffer)
            }

        } catch (e2: Exception) {
            e2.printStackTrace()
            throw e2
        } finally {
            try {
                bis.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    fun cancel() {
        cancel(false)
    }

    fun cancel(skipEvents: Boolean) {
        this.skipEvents = skipEvents
        try {
            socket.close()
            isConnectionPrepared = false
            listener.onConnectionCanceled()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    interface TransferEventsListener {

        fun onMessageReceived(message: String)
        fun onMessageSent(message: String)

        fun onConnectionPrepared(type: BluetoothConnectionService.ConnectionType)

        fun onConnectionCanceled()
        fun onConnectionLost()
    }
}
