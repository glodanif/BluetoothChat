package com.glodanif.bluetoothchat.data.entity

import android.arch.persistence.room.Entity
import android.arch.persistence.room.Ignore
import android.arch.persistence.room.PrimaryKey
import java.util.*

@Entity(tableName = "message")
data class ChatMessage(
        var deviceAddress: String,
        var date: Date,
        var own: Boolean,
        var text: String
) {

    @PrimaryKey(autoGenerate = true)
    var uid: Long = 0
    var seenHere: Boolean = false
    var seenThere: Boolean = false
    var delivered: Boolean = false
    var edited: Boolean = false

    var messageType: MessageType? = null
    var filePath: String? = null
    var fileInfo: String? = null

    @Ignore
    var fileExists: Boolean = false
}
