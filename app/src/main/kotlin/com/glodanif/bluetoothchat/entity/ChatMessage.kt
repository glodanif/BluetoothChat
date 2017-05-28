package com.glodanif.bluetoothchat.entity

import java.util.*

class ChatMessage {

    constructor(deviceAddress: String, date: Date, own: Boolean, text: String) {
        this.deviceAddress = deviceAddress
        this.date = date
        this.seen = seen
        this.own = own
        this.text = text
    }

    var deviceAddress: String? = null
    var date: Date? = null
    var seen: Boolean = false
    var own: Boolean = false
    var text: String? = null
}
