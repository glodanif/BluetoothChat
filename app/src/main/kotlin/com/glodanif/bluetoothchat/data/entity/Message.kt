package com.glodanif.bluetoothchat.data.entity

import android.support.annotation.ColorInt

class Message() {

    val DIVIDER = "#"

    var type: Type = Type.MESSAGE
    var uid: String = ""
    var flag: Boolean = false
    var body: String = ""

    constructor(message: String) : this() {

        var messageText = message

        type = Type.from(messageText.substring(0, messageText.indexOf(DIVIDER)).toInt())
        messageText = messageText.substring(messageText.indexOf(DIVIDER) + 1, messageText.length)

        uid = messageText.substring(0, messageText.indexOf(DIVIDER))
        messageText = messageText.substring(messageText.indexOf(DIVIDER) + 1, messageText.length)

        flag = messageText.substring(0, messageText.indexOf(DIVIDER)).toInt() == 1
        messageText = messageText.substring(messageText.indexOf(DIVIDER) + 1, messageText.length)

        body = messageText
    }

    constructor(uid: String, body: String, flag: Boolean, type: Type) : this() {
        this.uid = uid
        this.body = body
        this.type = type
        this.flag = flag
    }

    constructor(uid: String, body: String, type: Type) : this() {
        this.uid = uid
        this.body = body
        this.type = type
    }

    constructor(uid: String, flag: Boolean, type: Type) : this() {
        this.uid = uid
        this.flag = flag
        this.type = type
    }

    constructor(flag: Boolean, type: Type) : this() {
        this.flag = flag
        this.type = type
    }

    enum class Type(val value : Int) {
        MESSAGE(0),
        DELIVERY(1),
        CONNECTION_RESPONSE(2),
        CONNECTION_REQUEST(3),
        SEEING(4),
        EDITING(5);

        companion object {
            fun from(findValue: Int): Type = Type.values().first { it.value == findValue }
        }
    }

    fun getDecodedMessage(): String {
        val flag = if (this.flag) 1 else 0
        return "${type.value}$DIVIDER$uid$DIVIDER$flag$DIVIDER$body"
    }

    companion object {

        fun createConnectMessage(name: String, @ColorInt color: Int): Message {
            return Message("0", "$name#$color", true, Type.CONNECTION_REQUEST)
        }

        fun createDisconnectMessage(): Message {
            return Message("0", "", false, Type.CONNECTION_REQUEST)
        }

        fun createAcceptConnectionMessage(name: String, @ColorInt color: Int): Message {
            return Message("0", "$name#$color", true, Type.CONNECTION_RESPONSE)
        }

        fun createRejectConnectionMessage(name: String, @ColorInt color: Int) : Message{
            return Message("0", "$name#$color", false, Type.CONNECTION_RESPONSE)
        }
    }
}
