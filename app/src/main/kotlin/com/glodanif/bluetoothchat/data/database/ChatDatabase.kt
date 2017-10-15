package com.glodanif.bluetoothchat.data.database

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase
import android.arch.persistence.room.TypeConverters
import com.glodanif.bluetoothchat.data.entity.ChatMessage
import com.glodanif.bluetoothchat.data.entity.Conversation

@Database(entities = arrayOf(ChatMessage::class, Conversation::class), version = 1)
@TypeConverters(*arrayOf(Converter::class))
abstract class ChatDatabase: RoomDatabase() {

    abstract fun conversationsDao(): ConversationsDao

    abstract fun messagesDao(): MessagesDao
}