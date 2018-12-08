package com.glodanif.bluetoothchat.domain

sealed class FileTransferringStatus {
    object NotTransferring : FileTransferringStatus()
    class Receiving(val fileName: String?, val fileSize: Long) : FileTransferringStatus()
    class Sending(val fileName: String?, val fileSize: Long) : FileTransferringStatus()
}
