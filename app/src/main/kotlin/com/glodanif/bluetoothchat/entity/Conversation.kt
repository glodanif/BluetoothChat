package com.glodanif.bluetoothchat.entity

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import java.util.*

@Entity(tableName = "conversation")
data class Conversation(
        @PrimaryKey
        @ColumnInfo(name = "address")
        var deviceAddress: String,
        var deviceName: String,
        var displayName: String,
        var color: Int
) {
        @ColumnInfo(name = "date")
        var lastActivity: Date? = null
        @ColumnInfo(name = "text")
        var lastMessage: String? = null
}
