package com.glodanif.bluetoothchat.data.service

enum class PayloadType(val value: Int) {

    TEXT(0),
    IMAGE(1);

    companion object {
        fun from(findValue: Int): PayloadType = PayloadType.values().first { it.value == findValue }
    }
}
