package com.glodanif.bluetoothchat.data.service.connection

import android.bluetooth.BluetoothSocket
import com.glodanif.bluetoothchat.data.service.message.TransferringFile
import com.glodanif.bluetoothchat.utils.safeLet
import java.io.*
import kotlin.concurrent.thread

abstract class DataTransferThread(private val socket: BluetoothSocket,
                                  private val type: ConnectionType,
                                  private val transferListener: TransferEventsListener,
                                  private val filesDirectory: File,
                                  private val fileListener: OnFileListener,
                                  private val eventsStrategy: EventsStrategy) : Thread() {

    private var inputStream: InputStream? = null
    private var outputStream: OutputStream? = null

    private val bufferSize = 2048
    private val buffer = ByteArray(bufferSize)

    private var skipEvents = false

    @Volatile
    private var isConnectionPrepared = false
    @Volatile
    private var isFileTransferCanceledByMe = false
    @Volatile
    private var isFileTransferCanceledByPartner = false
    @Volatile
    private var isFileDownloading = false
    @Volatile
    private var isFileUploading = false
    @Volatile
    private var fileName: String? = null
    private var fileMessageId: Long = 0
    private var fileSize: Long = 0

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
        require(isConnectionPrepared) { "Connection is not prepared yet." }
        super.start()
    }

    override fun run() {

        while (shouldRun()) {

            try {

                readString()?.let { message ->

                    val potentialFile = eventsStrategy.isFileStart(message)
                    if (potentialFile != null) {

                        isFileDownloading = true
                        fileMessageId = potentialFile.uid
                        fileName = potentialFile.name
                        fileSize = potentialFile.size

                        transferListener.onMessageReceived(message)

                        safeLet(inputStream, fileName) { stream, name ->
                            readFile(stream, name, fileSize)
                        }

                    } else if (eventsStrategy.isMessage(message)) {

                        val cancelInfo = eventsStrategy.isFileCanceled(message)
                        if (cancelInfo == null) {
                            transferListener.onMessageReceived(message)
                        } else {
                            fileListener.onFileTransferCanceled(cancelInfo.byPartner)
                            isFileTransferCanceledByPartner = cancelInfo.byPartner
                        }
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

        return inputStream?.read(buffer)?.let {
            try {
                String(buffer, 0, it)
            } catch (e: StringIndexOutOfBoundsException) {
                null
            }
        }
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
        } catch (e: Exception) {
            transferListener.onMessageSendingFailed()
            e.printStackTrace()
        }
    }

    fun writeFile(uid: Long, file: File) {

        if (!file.exists()) {
            fileListener.onFileSendingFailed()
            resetFileTransferState()
            return
        }

        fileMessageId = uid
        fileName = file.absolutePath
        fileSize = file.length()

        isFileUploading = true
        isFileTransferCanceledByMe = false
        isFileTransferCanceledByPartner = false

        val transferringFile = TransferringFile(fileName, fileSize, TransferringFile.TransferType.SENDING)
        fileListener.onFileSendingStarted(transferringFile)

        thread {

            val fileStream = FileInputStream(file)
            BufferedInputStream(fileStream).use {

                val bufferedOutputStream = BufferedOutputStream(outputStream)

                try {

                    var sentBytes: Long = 0
                    var length: Int
                    val buffer = ByteArray(bufferSize)

                    length = it.read(buffer)
                    while (length > -1) {
                        if (length > 0) {
                            try {
                                bufferedOutputStream.write(buffer, 0, length)
                                bufferedOutputStream.flush()
                            } catch (e: IOException) {
                                Thread.sleep(200)
                                fileListener.onFileSendingFailed()
                                break
                            }
                            sentBytes += length.toLong()
                        }
                        length = it.read(buffer)

                        fileListener.onFileSendingProgress(transferringFile, sentBytes)

                        if (isFileTransferCanceledByMe || isFileTransferCanceledByPartner) {
                            bufferedOutputStream.flush()
                            break
                        }
                    }

                    Thread.sleep(250)

                    if (!isFileTransferCanceledByMe && !isFileTransferCanceledByPartner) {
                        fileListener.onFileSendingFinished(fileMessageId, file.absolutePath)
                    } else {
                        if (isFileTransferCanceledByMe) {
                            write(eventsStrategy.getCancellationMessage(true))
                        }
                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                    fileListener.onFileSendingFailed()
                } finally {
                    fileStream.close()
                    resetFileTransferState()
                }
            }
        }
    }

    private fun resetFileTransferState() {

        isFileUploading = false
        isFileTransferCanceledByMe = false
        isFileTransferCanceledByPartner = false

        fileName = null
        fileSize = 0
        fileMessageId = 0
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
        isFileTransferCanceledByMe = true
    }

    fun getTransferringFile(): TransferringFile? {

        return fileName?.let {
            when {
                isFileDownloading ->
                    TransferringFile(it, fileSize, TransferringFile.TransferType.RECEIVING)
                isFileUploading ->
                    TransferringFile(it, fileSize, TransferringFile.TransferType.SENDING)
                else -> null
            }
        }
    }

    private fun readFile(stream: InputStream, name: String, size: Long) {

        isFileTransferCanceledByMe = false
        isFileTransferCanceledByPartner = false

        if (!filesDirectory.exists()) {
            filesDirectory.mkdirs()
        }

        val file = File(filesDirectory, name)

        val bis = BufferedInputStream(stream)

        BufferedOutputStream(FileOutputStream(file)).use {

            val transferringFile = TransferringFile(name, size, TransferringFile.TransferType.RECEIVING)
            fileListener.onFileReceivingStarted(transferringFile)

            try {

                var bytesRead: Long = 0
                val buffer = ByteArray(bufferSize)
                var timeOut = 0
                val maxTimeOut = 16

                var isCanceled = false

                while (bytesRead < size) {

                    while (bis.available() == 0 && timeOut < maxTimeOut) {
                        timeOut++
                        Thread.sleep(250)
                    }

                    val remainingSize = size - bytesRead
                    val byteCount = Math.min(remainingSize, bufferSize.toLong()).toInt()

                    val len = bis.read(buffer, 0, byteCount)
                    val str = String(buffer, 0, byteCount)

                    if (eventsStrategy.isFileFinish(str)) {
                        break
                    }

                    val cancelInfo = eventsStrategy.isFileCanceled(str)
                    if (cancelInfo != null) {
                        isCanceled = true
                        fileListener.onFileTransferCanceled(cancelInfo.byPartner)
                        file.delete()
                        break
                    }

                    if (isFileTransferCanceledByMe || isFileTransferCanceledByPartner) {
                        it.flush()
                        break
                    }

                    if (len > 0) {
                        timeOut = 0
                        it.write(buffer, 0, len)
                        it.flush()
                        bytesRead += len.toLong()

                        fileListener.onFileReceivingProgress(transferringFile, bytesRead)
                    }
                }

                Thread.sleep(250)

                if (!isCanceled && !isFileTransferCanceledByMe && !isFileTransferCanceledByPartner) {
                    fileListener.onFileReceivingFinished(fileMessageId, file.absolutePath)
                }

            } catch (e: Exception) {
                fileListener.onFileReceivingFailed()
                throw e
            } finally {

                if (isFileTransferCanceledByMe || isFileTransferCanceledByPartner) {
                    isFileTransferCanceledByMe = false
                    isFileTransferCanceledByPartner = false
                    file.delete()
                    write(eventsStrategy.getCancellationMessage(true))
                }

                isFileDownloading = false
                fileName = null
                fileSize = 0
                fileMessageId = 0
            }
        }
    }

    interface TransferEventsListener {

        fun onMessageReceived(message: String)
        fun onMessageSent(message: String)
        fun onMessageSendingFailed()

        fun onConnectionPrepared(type: ConnectionType)

        fun onConnectionCanceled()
        fun onConnectionLost()
    }

    interface OnFileListener {
        fun onFileSendingStarted(file: TransferringFile)
        fun onFileSendingProgress(file: TransferringFile, sentBytes: Long)
        fun onFileSendingFinished(uid: Long, path: String)
        fun onFileSendingFailed()
        fun onFileReceivingStarted(file: TransferringFile)
        fun onFileReceivingProgress(file: TransferringFile, receivedBytes: Long)
        fun onFileReceivingFinished(uid: Long, path: String)
        fun onFileReceivingFailed()
        fun onFileTransferCanceled(byPartner: Boolean)
    }

    interface EventsStrategy {
        fun isMessage(message: String?): Boolean
        fun isFileStart(message: String?): FileInfo?
        fun isFileCanceled(message: String?): CancelInfo?
        fun isFileFinish(message: String?): Boolean
        fun getCancellationMessage(byPartner: Boolean): String
    }

    data class FileInfo(val uid: Long, val name: String, val size: Long)
    data class CancelInfo(val byPartner: Boolean)
}
