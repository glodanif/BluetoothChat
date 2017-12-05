package com.glodanif.bluetoothchat.data.database

import android.arch.persistence.room.TypeConverter
import com.glodanif.bluetoothchat.data.entity.Message
import com.glodanif.bluetoothchat.data.entity.MessageType
import java.util.*

class Converter {

    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return if (value == null) null else Date(value)
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time ?: 0
    }

    @TypeConverter
    fun toFileType(value: Int?): MessageType? {
        return if (value == null) MessageType.TEXT else MessageType.from(value)
    }

    @TypeConverter
    fun fromFileType(fileType: MessageType?): Int? {
        return fileType?.value ?: 0
    }
}
