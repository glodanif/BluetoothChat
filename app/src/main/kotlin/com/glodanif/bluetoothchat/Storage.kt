package com.glodanif.bluetoothchat

import android.arch.persistence.room.Room
import android.content.Context
import com.glodanif.bluetoothchat.database.ChatDatabase

class Storage private constructor(context: Context) {

    val db: ChatDatabase = Room.databaseBuilder(context,
            ChatDatabase::class.java, "chat_database").build()

    companion object {

        private var instance : Storage? = null

        fun getInstance(context: Context): Storage {
            if (instance == null)
                instance = Storage(context)

            return instance!!
        }
    }
}
