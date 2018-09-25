package com.glodanif.bluetoothchat.data.database

import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.room.Room
import androidx.room.migration.Migration
import android.content.Context

class Database {

    companion object {

        fun getInstance(context: Context) =
                Room.databaseBuilder(context, ChatDatabase::class.java, "chat_database")
                        .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                        .build()

        private val MIGRATION_1_2: Migration = object : Migration(1, 2) {

            override fun migrate(database: SupportSQLiteDatabase) = with(database) {

                execSQL("BEGIN TRANSACTION")

                execSQL("ALTER TABLE 'message' RENAME TO 'tmp_message'")
                execSQL("CREATE TABLE 'message' (" +
                        "'uid' INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "'deviceAddress' TEXT NOT NULL, " +
                        "'date' INTEGER NOT NULL, " +
                        "'own' INTEGER NOT NULL, " +
                        "'text' TEXT NOT NULL, " +
                        "'messageType' INTEGER, " +
                        "'filePath' TEXT, " +
                        "'fileInfo' TEXT, " +
                        "'seenHere' INTEGER NOT NULL DEFAULT 0, " +
                        "'seenThere' INTEGER NOT NULL DEFAULT 0, " +
                        "'delivered' INTEGER NOT NULL DEFAULT 0, " +
                        "'edited' INTEGER NOT NULL DEFAULT 0)")
                execSQL("INSERT INTO 'message' " +
                        "(uid, deviceAddress, date, own, text, seenHere, seenThere, edited) " +
                        "SELECT uid, deviceAddress, date, own, text, seenHere, seenThere, edited FROM 'tmp_message'")
                execSQL("DROP TABLE 'tmp_message'")

                execSQL("ALTER TABLE 'conversation' RENAME TO 'tmp_conversation'")
                execSQL("CREATE TABLE 'conversation' (" +
                        "'address' TEXT PRIMARY KEY NOT NULL, " +
                        "'deviceName' TEXT NOT NULL, " +
                        "'displayName' TEXT NOT NULL, " +
                        "'color' INTEGER NOT NULL, " +
                        "'date' INTEGER, " +
                        "'text' TEXT, " +
                        "'messageType' INTEGER, " +
                        "'notSeen' INTEGER NOT NULL DEFAULT 0)")
                execSQL("INSERT INTO 'conversation' " +
                        "(address, deviceName, displayName, color, date, text, notSeen) " +
                        "SELECT address, deviceName, displayName, color, date, text, notSeen FROM 'tmp_conversation'")
                execSQL("DROP TABLE 'tmp_conversation'")

                execSQL("COMMIT")
            }
        }

        private val MIGRATION_2_3: Migration = object : Migration(2, 3) {

            override fun migrate(database: SupportSQLiteDatabase) = with(database) {

                execSQL("BEGIN TRANSACTION")

                execSQL("ALTER TABLE 'conversation' RENAME TO 'tmp_conversation'")
                execSQL("CREATE TABLE 'conversation' (" +
                        "'address' TEXT PRIMARY KEY NOT NULL, " +
                        "'deviceName' TEXT NOT NULL, " +
                        "'displayName' TEXT NOT NULL, " +
                        "'color' INTEGER NOT NULL)")
                execSQL("INSERT INTO 'conversation' " +
                        "(address, deviceName, displayName, color) " +
                        "SELECT address, deviceName, displayName, color FROM 'tmp_conversation'")
                execSQL("DROP TABLE 'tmp_conversation'")

                execSQL("COMMIT")
            }
        }
    }
}
