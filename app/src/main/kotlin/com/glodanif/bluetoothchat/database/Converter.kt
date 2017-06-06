package com.glodanif.bluetoothchat.database

import android.arch.persistence.room.TypeConverter
import java.util.*

class Converter {

    companion object {

        @JvmStatic
        @TypeConverter
        fun fromTimestamp(value: Long?): Date? {
            return if (value == null) null else Date(value)
        }

        @JvmStatic
        @TypeConverter
        fun dateToTimestamp(date: Date?): Long {
            return date?.time ?: 0
        }
    }
}