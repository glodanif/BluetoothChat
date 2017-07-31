package com.glodanif.bluetoothchat.database

import android.arch.persistence.room.*
import com.glodanif.bluetoothchat.entity.Conversation

@Dao
interface ConversationsDao {

    @Query("SELECT * FROM conversation")
    fun getAllConversations(): List<Conversation>

    @Query("SELECT conversation.*, message.*, " +
            "(SELECT COUNT(message.seenHere) FROM message WHERE conversation.address = message.deviceAddress AND message.seenHere = 0) AS notSeen, " +
            "(SELECT MAX(message.date) FROM message WHERE conversation.address = message.deviceAddress) AS lastActivity " +
            "FROM conversation LEFT JOIN message ON conversation.address = message.deviceAddress AND " +
            "message.date = lastActivity AND conversation.notSeen = notSeen GROUP BY message.date ORDER BY message.date DESC")
    fun getAllConversationsWithMessages(): List<Conversation>

    @Query("SELECT * FROM conversation WHERE address = :address")
    fun getConversationByAddress(address: String): Conversation?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(conversations: Conversation)

    @Delete
    fun delete(conversations: Conversation)
}
