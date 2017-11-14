package com.glodanif.bluetoothchat.data.database

import android.arch.persistence.db.SupportSQLiteDatabase
import android.arch.persistence.room.Room
import android.arch.persistence.room.migration.Migration
import android.content.Context

class Storage private constructor(context: Context) {

    val db: ChatDatabase = Room.databaseBuilder(context,
            ChatDatabase::class.java, "chat_database")
            .addMigrations(MIGRATION_1_2)
            .build()

    companion object {

        private val MIGRATION_1_2: Migration = object : Migration(1, 2) {

            override fun migrate(database: SupportSQLiteDatabase) {

                // updated Room from v1.0.0-alpha7 to v1.0.0, it messed up with nullability of some columns

                database.execSQL("BEGIN TRANSACTION")

                database.execSQL("ALTER TABLE 'message' RENAME TO 'tmp_message'")
                database.execSQL("CREATE TABLE 'message' (" +
                        "'uid' INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "'deviceAddress' TEXT NOT NULL, " +
                        "'date' INTEGER NOT NULL, " +
                        "'own' INTEGER NOT NULL, " +
                        "'text' TEXT NOT NULL, " +
                        "'seenHere' INTEGER NOT NULL DEFAULT 0, " +
                        "'seenThere' INTEGER NOT NULL DEFAULT 0, " +
                        "'delivered' INTEGER NOT NULL DEFAULT 0, " +
                        "'edited' INTEGER NOT NULL DEFAULT 0)")
                database.execSQL("INSERT INTO 'message' " +
                        "(uid, deviceAddress, date, own, text, seenHere, seenThere, edited) " +
                        "SELECT uid, deviceAddress, date, own, text, seenHere, seenThere, edited FROM 'tmp_message'")
                database.execSQL("DROP TABLE 'tmp_message'")

                database.execSQL("ALTER TABLE 'conversation' RENAME TO 'tmp_conversation'")
                database.execSQL("CREATE TABLE 'conversation' (" +
                        "'address' TEXT PRIMARY KEY NOT NULL, " +
                        "'deviceName' TEXT NOT NULL, " +
                        "'displayName' TEXT NOT NULL, " +
                        "'color' INTEGER NOT NULL, " +
                        "'date' INTEGER, " +
                        "'text' TEXT, " +
                        "'notSeen' INTEGER NOT NULL DEFAULT 0)")
                database.execSQL("INSERT INTO 'conversation' " +
                        "(address, deviceName, displayName, color, date, text, notSeen) " +
                        "SELECT address, deviceName, displayName, color, date, text, notSeen FROM 'tmp_conversation'")
                database.execSQL("DROP TABLE 'tmp_conversation'")

                database.execSQL("COMMIT")
            }
        }

        private var instance : Storage? = null

        fun getInstance(context: Context): Storage {
            if (instance == null)
                instance = Storage(context)

            return instance!!
        }
    }
}
