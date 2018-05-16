package com.glodanif.bluetoothchat.data.database

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Delete
import android.arch.persistence.room.Insert
import android.arch.persistence.room.Query
import com.glodanif.bluetoothchat.data.entity.ChatMessage
import android.arch.persistence.room.Update
import com.glodanif.bluetoothchat.data.entity.MessageFile

@Dao
interface MessagesDao {

    @Query("SELECT * FROM message WHERE deviceAddress = :address ORDER BY date DESC")
    fun getMessagesByDevice(address: String): List<ChatMessage>

    @Query("SELECT uid, filePath, own FROM message WHERE deviceAddress = :address AND messageType = 1 AND own = 0 AND filePath IS NOT NULL ORDER BY date DESC")
    fun getFileMessagesByDevice(address: String): List<MessageFile>

    @Query("SELECT uid, filePath, own FROM message WHERE uid = :uid")
    fun getFileMessageById(uid: Long): MessageFile?

    @Query("SELECT uid, filePath, own FROM message WHERE messageType = 1 AND own = 0 AND filePath IS NOT NULL ORDER BY date DESC")
    fun getAllFilesMessages(): List<MessageFile>

    @Insert
    fun insert(message: ChatMessage)

    @Update
    fun updateMessages(messages: List<ChatMessage>)

    @Update
    fun updateMessage(message: ChatMessage)

    @Delete
    fun delete(message: ChatMessage)

    @Query("DELETE FROM message WHERE deviceAddress = :address")
    fun deleteAllByDeviceAddress(address: String)

    @Query("UPDATE message SET fileInfo = null, filePath = null WHERE uid = :messageId")
    fun removeFileInfo(messageId: Long)
}
