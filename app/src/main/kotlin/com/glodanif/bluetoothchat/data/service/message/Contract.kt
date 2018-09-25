package com.glodanif.bluetoothchat.data.service.message

import androidx.annotation.ColorInt
import java.io.File

class Contract {

    var partnerVersion: Int = MESSAGE_CONTRACT_VERSION

    infix fun setupWith(version: Int) {
        partnerVersion = version
    }

    fun reset() {
        partnerVersion = MESSAGE_CONTRACT_VERSION
    }

    fun createChatMessage(message: String): Message {
        return Message(generateUniqueId(), message, MessageType.MESSAGE)
    }

    fun createConnectMessage(name: String, @ColorInt color: Int): Message {
        return Message(0, "$name#$color#$MESSAGE_CONTRACT_VERSION", true, MessageType.CONNECTION_REQUEST)
    }

    fun createDisconnectMessage(): Message {
        return Message(0, "", false, MessageType.CONNECTION_REQUEST)
    }

    fun createAcceptConnectionMessage(name: String, @ColorInt color: Int): Message {
        return Message(0, "$name#$color#$MESSAGE_CONTRACT_VERSION", true, MessageType.CONNECTION_RESPONSE)
    }

    fun createRejectConnectionMessage(name: String, @ColorInt color: Int): Message {
        return Message(0, "$name#$color#$MESSAGE_CONTRACT_VERSION", false, MessageType.CONNECTION_RESPONSE)
    }

    fun createFileStartMessage(file: File, type: PayloadType): Message {
        val uid = if (partnerVersion >= 2) generateUniqueId() else 0L
        return Message(uid, "${file.name.replace("#", "")}#${file.length()}#${type.value}", false, MessageType.FILE_START)
    }

    fun createFileEndMessage(): Message {
        return Message(0, "", false, MessageType.FILE_START)
    }

    fun isFeatureAvailable(feature: Feature) = when (feature) {
        Feature.IMAGE_SHARING -> partnerVersion >= 1
    }

    enum class MessageType(val value: Int) {

        UNEXPECTED(-1),
        MESSAGE(0),
        DELIVERY(1),
        CONNECTION_RESPONSE(2),
        CONNECTION_REQUEST(3),
        SEEING(4),
        EDITING(5),
        FILE_START(6),
        FILE_END(7),
        FILE_CANCELED(8);

        companion object {
            fun from(findValue: Int) = values().first { it.value == findValue }
        }
    }

    enum class Feature {
        IMAGE_SHARING;
    }

    companion object {

        const val DIVIDER = "#"
        const val MESSAGE_CONTRACT_VERSION = 2

        fun generateUniqueId() = System.nanoTime()
    }
}
