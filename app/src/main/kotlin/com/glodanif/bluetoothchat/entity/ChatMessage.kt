package com.glodanif.bluetoothchat.entity

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import java.util.*

@Entity(tableName = "message")
data class ChatMessage(
        @ColumnInfo(name = "address") var deviceAddress: String,
        var date: Date,
        var own: Boolean,
        var text: String
) {

    @PrimaryKey(autoGenerate = true)
    var uid: Long = 0
    var seen: Boolean = false
    var edited: Boolean = false
}
