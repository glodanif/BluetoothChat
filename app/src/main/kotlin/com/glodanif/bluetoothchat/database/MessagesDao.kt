package com.glodanif.bluetoothchat.database

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Delete
import android.arch.persistence.room.Insert
import android.arch.persistence.room.Query
import com.glodanif.bluetoothchat.entity.ChatMessage

@Dao
interface MessagesDao {

    @Query("SELECT * FROM message WHERE address = :p0 ORDER BY date DESC")
    fun getMessagesByDevice(address: String): List<ChatMessage>

    @Insert
    fun insert(message: ChatMessage)

    @Delete
    fun delete(message: ChatMessage)
}
