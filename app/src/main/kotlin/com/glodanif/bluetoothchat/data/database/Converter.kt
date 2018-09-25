package com.glodanif.bluetoothchat.data.database

import androidx.room.TypeConverter
import com.glodanif.bluetoothchat.data.service.message.PayloadType
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
    fun toFileType(value: Int?): PayloadType? {
        return if (value == null) PayloadType.TEXT else PayloadType.from(value)
    }

    @TypeConverter
    fun fromFileType(fileType: PayloadType?): Int? {
        return fileType?.value ?: 0
    }
}
