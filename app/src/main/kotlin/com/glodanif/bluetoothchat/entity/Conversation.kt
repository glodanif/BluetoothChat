package com.glodanif.bluetoothchat.entity

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

@Entity(tableName = "conversation")
data class Conversation(
        @PrimaryKey
        @ColumnInfo(name = "address") var deviceAddress: String,
        var deviceName: String
)
