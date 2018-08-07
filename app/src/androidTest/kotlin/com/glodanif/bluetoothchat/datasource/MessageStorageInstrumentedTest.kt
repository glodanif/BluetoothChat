package com.glodanif.bluetoothchat.datasource

import android.os.Environment
import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import com.glodanif.bluetoothchat.R
import com.glodanif.bluetoothchat.data.database.Database
import com.glodanif.bluetoothchat.data.entity.ChatMessage
import com.glodanif.bluetoothchat.data.model.MessagesStorage
import com.glodanif.bluetoothchat.data.model.MessagesStorageImpl
import com.glodanif.bluetoothchat.data.service.message.PayloadType
import kotlinx.coroutines.experimental.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.util.*
import android.support.test.rule.GrantPermissionRule
import org.junit.Rule

@RunWith(AndroidJUnit4::class)
class MessageStorageInstrumentedTest {

    private lateinit var storage: MessagesStorage

    private val address1 = "00:00:00:00:00:01"
    private val date = Date(1533585790)
    private val updatedDate = Date(1533585808)
    private val message1 = ChatMessage(address1, date, true,"text1").apply { uid = 1 }
    private val message2 = ChatMessage(address1, date, false,"text2").apply { uid = 2 }
    private val editedMessage2 = ChatMessage(address1, updatedDate, false,"text2_2").apply { uid = 2 }

    private val address2 = "00:00:00:00:00:02"
    private val message3 = ChatMessage(address2, date, false,"text1").apply {
        uid = 3
        messageType = PayloadType.IMAGE
        fileInfo = "300x300"
    }
    private val message4 = ChatMessage(address2, date, false,"text2").apply {
        uid = 4
        messageType = PayloadType.IMAGE
        filePath = "randomPath1.jpg"
        fileInfo = "300x300"
    }
    private val message5 = ChatMessage(address2, date, false,"text3").apply {
        uid = 5
        messageType = PayloadType.IMAGE
        filePath = "randomPath2.jpg"
        fileInfo = "300x300"
    }
    private val message6 = ChatMessage(address2, date, true,"text4").apply {
        uid = 6
        messageType = PayloadType.IMAGE
        fileInfo = "300x300"
    }
    private val message7 = ChatMessage(address2, date, false,"text5").apply { uid = 7 }
    private val editedMessage7 = ChatMessage(address2, updatedDate, false,"text5_2").apply { uid = 7 }

    private val address3 = "00:00:00:00:00:03"
    private val message8 = ChatMessage(address3, date, true,"text1").apply { uid = 8 }

    private lateinit var file1: File
    private lateinit var file2: File

    @Rule
    @JvmField
    var permissionRule: GrantPermissionRule =
            GrantPermissionRule.grant(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)

    @Before
    fun prepare() = runBlocking {

        val context = InstrumentationRegistry.getTargetContext()

        val directory = context.externalCacheDir
                ?: File(Environment.getExternalStorageDirectory(), context.getString(R.string.app_name))
        file1 = File(directory, "file1.jpg").apply {
            createNewFile()
        }
        file2 = File(directory, "file2.jpg").apply {
            createNewFile()
        }

        message3.filePath = file1.absolutePath
        message6.filePath = file2.absolutePath

        storage = MessagesStorageImpl(Database.getInstance(context)).apply {
            insertMessage(message1)
            insertMessage(message2)
            insertMessage(message3)
            insertMessage(message4)
            insertMessage(message5)
            insertMessage(message6)
            insertMessage(message7)
            insertMessage(message8)
        }
    }

    @After
    fun release() = runBlocking {
        file1.delete()
        file2.delete()
        storage.removeMessagesByAddress(address1)
        storage.removeMessagesByAddress(address2)
        storage.removeMessagesByAddress(address3)
    }

    @Test
    fun insertMessage() = runBlocking {
        storage.removeMessagesByAddress(address3)
        storage.insertMessage(message8)
        val messages = storage.getMessagesByDevice(address3)
        assertNotNull(messages.lastOrNull { it.uid == 8L })
    }

    @Test
    fun removeMessages() = runBlocking {
        storage.removeMessagesByAddress(address3)
        storage.insertMessage(message8)
        storage.removeMessagesByAddress(address3)
        val messages = storage.getMessagesByDevice(address3)
        assertNull(messages.lastOrNull { it.uid == 8L })
    }

    @Test
    fun updateMessage() = runBlocking {
        storage.updateMessage(editedMessage2)
        val message = storage.getMessagesByDevice(address1).lastOrNull { it.uid == 2L }
        assertNotNull(message)
        assertTrue(equal(message, editedMessage2))
    }

    @Test
    fun updateMessages() = runBlocking {
        storage.updateMessages(listOf(editedMessage7))
        val message = storage.getMessagesByDevice(address2).lastOrNull { it.uid == 7L }
        assertNotNull(message)
        assertTrue(equal(message, editedMessage7))
    }

    @Test
    fun removeFileInfo() = runBlocking {
        storage.removeFileInfo(4)
        val message = storage.getMessagesByDevice(address2).lastOrNull { it.uid == 4L }
        assertNotNull(message)
        assertTrue(message?.fileInfo == null)
        assertTrue(message?.filePath == null)
    }

    @Test
    fun getMessagesByDevice() = runBlocking {
        val messages = storage.getMessagesByDevice(address1)
        assertTrue(messages.size == 2)
        assertTrue(messages.contains(message1))
        assertTrue(messages.contains(message2) || messages.contains(editedMessage2))
    }

    @Test
    fun getFileMessageById() = runBlocking {
        val message = storage.getFileMessageById(3L)
        assertNotNull(message)
        assertTrue(message?.uid == 3L)
        assertTrue(message?.filePath == message3.filePath)
        assertTrue(message?.own == message3.own)
    }

    @Test
    fun getFileMessagesByDevice_existingNotOwn() = runBlocking {
        val messages = storage.getFileMessagesByDevice(address2)
        val message = messages.lastOrNull { it.uid == 3L }
        assertNotNull(message)
        assertTrue(message?.uid == 3L)
        assertTrue(message?.filePath == message3.filePath)
        assertTrue(message?.own == message3.own)
    }

    @Test
    fun getFileMessagesByDevice_nonExistingNotOwn() = runBlocking {
        val messages = storage.getFileMessagesByDevice(address2)
        val message = messages.lastOrNull { it.uid == 5L }
        assertNull(message)
    }

    @Test
    fun getFileMessagesByDevice_existingOwn() = runBlocking {
        val messages = storage.getFileMessagesByDevice(address2)
        val message = messages.lastOrNull { it.uid == 6L }
        assertNull(message)
    }

    private fun equal(m1: ChatMessage?, m2: ChatMessage?) = m1 != null && m2 != null &&
            m1.uid == m2.uid && m1.date == m2.date && m1.deviceAddress == m2.deviceAddress &&
            m1.own == m2.own && m1.text == m2.text
}
