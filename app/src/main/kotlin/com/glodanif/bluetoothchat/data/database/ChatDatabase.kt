package com.glodanif.bluetoothchat.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.glodanif.bluetoothchat.data.entity.ChatMessage
import com.glodanif.bluetoothchat.data.entity.Conversation

@Database(entities = [(ChatMessage::class), (Conversation::class)], version = 3)
@TypeConverters(Converter::class)
abstract class ChatDatabase: RoomDatabase() {
    abstract fun conversationsDao(): ConversationsDao
    abstract fun messagesDao(): MessagesDao
}
