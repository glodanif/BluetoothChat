package com.glodanif.bluetoothchat.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "conversation")
data class Conversation(
        @PrimaryKey
        @ColumnInfo(name = "address")
        var deviceAddress: String,
        var deviceName: String,
        var displayName: String,
        var color: Int
)
