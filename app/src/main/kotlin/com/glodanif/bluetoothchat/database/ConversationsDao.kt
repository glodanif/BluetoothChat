package com.glodanif.bluetoothchat.database

import android.arch.persistence.room.*
import com.glodanif.bluetoothchat.entity.Conversation

@Dao
interface ConversationsDao {

    @Query("SELECT * FROM conversation")
    fun getAllConversations(): List<Conversation>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(conversations: Conversation)

    @Delete
    fun delete(conversations: Conversation)
}
