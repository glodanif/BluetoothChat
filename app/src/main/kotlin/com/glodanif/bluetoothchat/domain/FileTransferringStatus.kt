package com.glodanif.bluetoothchat.domain

import com.glodanif.bluetoothchat.data.service.message.TransferringFile

sealed class FileTransferringStatus {
    object NotTransferring : FileTransferringStatus()
    class Transferring(val fileName: String?, val fileSize: Long, val transferType: TransferringFile.TransferType) : FileTransferringStatus()
}
