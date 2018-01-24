package com.glodanif.bluetoothchat.data.entity

enum class MessageType(val value: Int) {

    TEXT(0),
    IMAGE(1);

    companion object {
        fun from(findValue: Int): MessageType = MessageType.values().first { it.value == findValue }
    }
}
