package com.glodanif.bluetoothchat.data.entity

data class TransferringFile(val name: String?, val size: Long, val transferType: TransferType) {
    enum class TransferType { SENDING, RECEIVING }
}
