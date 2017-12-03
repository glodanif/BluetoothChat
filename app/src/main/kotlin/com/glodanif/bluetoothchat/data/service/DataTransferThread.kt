package com.glodanif.bluetoothchat.data.service

import android.bluetooth.BluetoothSocket
import android.content.Context
import android.util.Log
import java.io.*
import kotlin.concurrent.thread

abstract class DataTransferThread(private val context: Context, private val socket: BluetoothSocket,
                                  private val type: BluetoothConnectionService.ConnectionType,
                                  private val transferListener: TransferEventsListener,
                                  private val fileListener: OnFileListener) : Thread() {

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
            transferListener.onConnectionPrepared(type)
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

                    transferListener.onMessageReceived(message)

                    readFile(inputStream!!, fileName!!, fileSize)

                } else {
                    if (message != null && message.contains("#")) {
                        transferListener.onMessageReceived(message)
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
                if (!skipEvents) {
                    transferListener.onConnectionLost()
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
            transferListener.onMessageSent(message)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun writeFile(file: File) {

        fileListener.onFileSendingStarted(file)

        thread {

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

                    fileListener.onFileSendingProgress(sentBytes, file.length())
                }

                fileListener.onFileSendingFinished()

            } catch (e2: Exception) {
                e2.printStackTrace()
                fileListener.onFileSendingFailed()
                throw e2
            } finally {
                try {
                    bis.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
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
            transferListener.onConnectionCanceled()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun readFile(stream: InputStream, name: String, size: Long) {

        val file = File(context.filesDir, name)

        val bis = BufferedInputStream(stream)
        val bos = BufferedOutputStream(FileOutputStream(file))

        fileListener.onFileReceivingStarted()

        try {

            var bytesRead: Long = 0
            var len = 0
            val bufSize = 1024
            val buffer = ByteArray(bufSize)
            var timeOut = 0
            val maxTimeOut = 16

            while (bytesRead < size) {
                Log.w("TAG", "BEFORE AVAILABLE " + bytesRead)
                while (bis.available() == 0 && timeOut < maxTimeOut) {
                    timeOut++
                    Thread.sleep(250)
                }

                val remainingSize = size - bytesRead
                val byteCount = Math.min(remainingSize, bufSize.toLong()).toInt()
                Log.w("TAG", "BEFORE READ " + "currentSize : "
                        + bytesRead + " byteCount " + byteCount)

                len = bis.read(buffer, 0, byteCount)

                Log.w("TAG", "AFTER READ " + "Len " + len)
                if (len > 0) {
                    timeOut = 0
                    Log.w("TAG", "BEFORE WRITE " + bytesRead)
                    bos.write(buffer, 0, len)
                    bytesRead += len.toLong()
                    Log.w("TAG", "AFTER WRITE " + bytesRead)

                    fileListener.onFileReceivingProgress(bytesRead, size)
                }
            }
            bos.flush()

            fileListener.onFileReceivingFinished()

        } catch (e: Exception) {
            Log.e("TAG", "Receiving problem")
            fileListener.onFileReceivingFailed()
            throw e
        } finally {
            try {
                Log.i("TAG", "FILE CLOSE")
                bos.close()
            } catch (e: IOException) {
            }
        }
    }

    interface TransferEventsListener {

        fun onMessageReceived(message: String)
        fun onMessageSent(message: String)

        fun onConnectionPrepared(type: BluetoothConnectionService.ConnectionType)

        fun onConnectionCanceled()
        fun onConnectionLost()
    }

    interface OnFileListener {
        fun onFileSendingStarted(file: File)
        fun onFileSendingProgress(sentBytes: Long, totalBytes: Long)
        fun onFileSendingFinished()
        fun onFileSendingCanceled()
        fun onFileSendingFailed()
        fun onFileReceivingStarted()
        fun onFileReceivingProgress(receivedBytes: Long, totalBytes: Long)
        fun onFileReceivingFinished()
        fun onFileReceivingCanceled()
        fun onFileReceivingFailed()
    }
}
