package com.glodanif.bluetoothchat.data.service

data class TransferringFile(val name: String?, val size: Long, val transferType: TransferType) {
    enum class TransferType { SENDING, RECEIVING }
}
