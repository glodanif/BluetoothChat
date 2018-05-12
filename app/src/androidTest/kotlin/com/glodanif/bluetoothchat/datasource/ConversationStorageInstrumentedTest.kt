package com.glodanif.bluetoothchat.datasource

import android.graphics.Color
import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import com.glodanif.bluetoothchat.data.entity.Conversation
import com.glodanif.bluetoothchat.data.model.ConversationsStorage
import com.glodanif.bluetoothchat.data.model.ConversationsStorageImpl
import kotlinx.coroutines.experimental.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ConversationStorageInstrumentedTest {

    private lateinit var storage: ConversationsStorage

    private val address1 = "00:00:00:00:00:01"
    private val conversation1 = Conversation(address1, "deviceName1", "displayName1", Color.BLACK)
    private val editedConversation1 = Conversation(address1, "deviceName1_1", "displayName1_1", Color.GREEN)

    private val address2 = "00:00:00:00:00:02"
    private val conversation2 = Conversation(address2, "deviceName2", "displayName2", Color.WHITE)

    private val address3 = "00:00:00:00:00:03"
    private val conversation3 = Conversation(address3, "deviceName3", "displayName3", Color.GRAY)

    @Before
    fun prepare() = runBlocking {
        val context = InstrumentationRegistry.getTargetContext()
        storage = ConversationsStorageImpl(context)
        storage.insertConversation(conversation1)
        storage.insertConversation(conversation2)
    }

    @After
    fun release() = runBlocking {
        storage.removeConversationByAddress(address1)
        storage.removeConversationByAddress(address2)
        storage.removeConversationByAddress(address3)
    }

    @Test
    fun insertConversation() = runBlocking {
        storage.insertConversation(conversation3)
        val dbConversation: Conversation? = storage.getConversationByAddress(address3)
        assertNotNull(dbConversation)
        assertTrue(equal(conversation3, dbConversation))
    }

    @Test
    fun updateConversation() = runBlocking {
        storage.insertConversation(editedConversation1)
        val dbConversation: Conversation? = storage.getConversationByAddress(address1)
        assertNotNull(dbConversation)
        assertTrue(equal(editedConversation1, dbConversation))
    }

    @Test
    fun removeConversation() = runBlocking {
        storage.removeConversationByAddress(address2)
        val dbConversation: Conversation? = storage.getConversationByAddress(address2)
        assertNull(dbConversation)
    }

    @Test
    fun getConversation_existingAddress() = runBlocking {
        val dbConversation: Conversation? = storage.getConversationByAddress(address1)
        assertNotNull(dbConversation)
        assertTrue(equal(conversation1, dbConversation))
    }

    @Test
    fun getConversation_nonExistingAddress() = runBlocking {
        val dbConversation: Conversation? = storage.getConversationByAddress("abcd")
        assertNull(dbConversation)
    }

    private fun equal(c1: Conversation?, c2: Conversation?) = c1 != null && c2 != null &&
            c1.deviceAddress == c2.deviceAddress && c1.deviceName == c2.deviceName &&
            c1.displayName == c2.displayName && c1.color == c2.color
}
