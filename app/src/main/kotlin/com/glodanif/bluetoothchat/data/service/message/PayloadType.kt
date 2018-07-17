package com.glodanif.bluetoothchat.data.service.message

enum class PayloadType(val value: Int) {

    TEXT(0),
    IMAGE(1);

    companion object {
        fun from(findValue: Int) = values().first { it.value == findValue }
    }
}
