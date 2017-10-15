package com.glodanif.bluetoothchat.data.database

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Delete
import android.arch.persistence.room.Insert
import android.arch.persistence.room.Query
import com.glodanif.bluetoothchat.data.entity.ChatMessage
import android.arch.persistence.room.Update

@Dao
interface MessagesDao {

    @Query("SELECT * FROM message WHERE deviceAddress = :address ORDER BY date DESC")
    fun getMessagesByDevice(address: String): List<ChatMessage>

    @Insert
    fun insert(message: ChatMessage)

    @Update
    fun updateMessages(messages: List<ChatMessage>)

    @Delete
    fun delete(message: ChatMessage)

    @Query("DELETE FROM message WHERE deviceAddress = :address")
    fun deleteAllByDeviceAddress(address: String)
}
