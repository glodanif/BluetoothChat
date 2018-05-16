package com.glodanif.bluetoothchat.data.entity

import java.util.*

data class SimpleChatMessage(
        var deviceAddress: String,
        var date: Date,
        var text: String,
        var seenHere: Boolean,
        var messageType: MessageType?
)
