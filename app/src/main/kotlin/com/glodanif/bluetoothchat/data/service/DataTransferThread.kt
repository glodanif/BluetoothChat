package com.glodanif.bluetoothchat.data.service

import android.bluetooth.BluetoothSocket
import android.content.Context
import android.util.Log
import java.io.*
import kotlin.concurrent.thread

abstract class DataTransferThread(private val context: Context, private val socket: BluetoothSocket,
                                  private val type: BluetoothConnectionService.ConnectionType,
                                  private val transferListener: TransferEventsListener,
                                  private val fileListener: OnFileListener, private var eventsStrategy: EventsStrategy) : Thread() {

    private val bufferSize = 1024

    private var inputStream: InputStream? = null
    private var outputStream: OutputStream? = null

    private var skipEvents = false

    private val buffer = ByteArray(bufferSize)
    private var bytes: Int? = null

    private var isConnectionPrepared = false

    @Volatile
    var isFileTransferCanceled = false
    @Volatile
    var isFileDownloading = false
    @Volatile
    var isFileUploading = false
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
                val potentialFile = eventsStrategy.isFileStart(message)

                if (message != null && potentialFile != null) {

                    isFileDownloading = true
                    fileName = potentialFile.name
                    fileSize = potentialFile.size

                    transferListener.onMessageReceived(message)
                    readFile(inputStream!!, fileName!!, fileSize)

                } else {
                    if (message != null && eventsStrategy.isMessage(message)) {
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

        isFileUploading = true

        fileListener.onFileSendingStarted(file)

        thread {

            val fileStream = FileInputStream(file)
            BufferedInputStream(fileStream).use {

                val bos = BufferedOutputStream(outputStream)

                try {

                    var sentBytes: Long = 0
                    var length: Int
                    val buffer = ByteArray(bufferSize)

                    length = it.read(buffer)
                    while (length > -1) {
                        if (length > 0) {
                            bos.write(buffer, 0, length)
                            bos.flush()
                            sentBytes += length.toLong()
                        }
                        length = it.read(buffer)

                        fileListener.onFileSendingProgress(sentBytes, file.length())
                    }

                    fileListener.onFileSendingFinished(file.absolutePath)

                } catch (e: Exception) {
                    e.printStackTrace()
                    fileListener.onFileSendingFailed()
                    throw e
                } finally {
                    isFileUploading = false
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

    fun cancelFileTransfer() {
        isFileTransferCanceled = true
    }

    private fun readFile(stream: InputStream, name: String, size: Long) {

        val file = File(context.filesDir, name)

        val bis = BufferedInputStream(stream)

        BufferedOutputStream(FileOutputStream(file)).use {

            fileListener.onFileReceivingStarted(size)

            try {

                var bytesRead: Long = 0
                var len = 0
                val buffer = ByteArray(bufferSize)
                var timeOut = 0
                val maxTimeOut = 16

                var isCanceled = false

                while (bytesRead < size) {
                    Log.w("TAG", "BEFORE AVAILABLE " + bytesRead)
                    while (bis.available() == 0 && timeOut < maxTimeOut) {
                        timeOut++
                        Thread.sleep(250)
                    }

                    val remainingSize = size - bytesRead
                    val byteCount = Math.min(remainingSize, bufferSize.toLong()).toInt()
                    Log.w("TAG", "BEFORE READ " + "currentSize : "
                            + bytesRead + " byteCount " + byteCount)

                    len = bis.read(buffer, 0, byteCount)

                    val str = String(buffer, 0, byteCount)

                    if (eventsStrategy.isFileFinish(str)) {
                        break
                    }

                    if (eventsStrategy.isFileCanceled(str)) {
                        isCanceled = true
                        break
                    }

                    Log.w("TAG", "AFTER READ " + "Len " + len)
                    if (len > 0) {
                        timeOut = 0
                        Log.w("TAG", "BEFORE WRITE " + bytesRead)
                        it.write(buffer, 0, len)
                        bytesRead += len.toLong()
                        Log.w("TAG", "AFTER WRITE " + bytesRead)

                        fileListener.onFileReceivingProgress(bytesRead, size)
                    }
                }
                it.flush()

                if (!isCanceled) {
                    fileListener.onFileReceivingFinished(file.absolutePath)
                } else {
                    fileListener.onFileReceivingCanceled()
                }

            } catch (e: Exception) {
                Log.e("TAG", "Receiving problem")
                fileListener.onFileReceivingFailed()
                throw e
            } finally {
                isFileDownloading = false
                fileName = null
                fileSize = 0
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
        fun onFileSendingFinished(filePath: String)
        fun onFileSendingCanceled()
        fun onFileSendingFailed()
        fun onFileReceivingStarted(fileSize: Long)
        fun onFileReceivingProgress(receivedBytes: Long, totalBytes: Long)
        fun onFileReceivingFinished(filePath: String)
        fun onFileReceivingCanceled()
        fun onFileReceivingFailed()
    }

    interface EventsStrategy {
        fun isMessage(message: String?): Boolean
        fun isFileStart(message: String?): FileInfo?
        fun isFileCanceled(message: String?): Boolean
        fun isFileFinish(message: String?): Boolean
    }

    data class FileInfo(val name: String, val size: Long)
}
