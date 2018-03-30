package com.glodanif.bluetoothchat

import android.graphics.Color
import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import android.util.Log
import com.glodanif.bluetoothchat.data.entity.Conversation
import com.glodanif.bluetoothchat.data.model.ConversationsStorage
import com.glodanif.bluetoothchat.data.model.ConversationsStorageImpl
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.runBlocking
import org.junit.After
import org.junit.Assert
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ConversationStorageInstrumentedTest {

    private lateinit var storage: ConversationsStorage

    private val address1 = "00:00:00:00:00:01"
    private val deviceName1 = "deviceName1"
    private val displayName1 = "displayName1"
    private val color1 = Color.BLACK
    private val conversation1 = Conversation(address1, deviceName1, displayName1, color1)

    private val address2 = "00:00:00:00:00:02"
    private val deviceName2 = "deviceName2"
    private val displayName2 = "displayName2"
    private val color2 = Color.WHITE
    private val conversation2 = Conversation(address2, deviceName2, displayName2, color2)

    private val address3 = "00:00:00:00:00:03"
    private val deviceName3 = "deviceName3"
    private val displayName3 = "displayName3"
    private val color3 = Color.GRAY
    private val conversation3 = Conversation(address3, deviceName3, displayName3, color3)

    @Before
    fun setup() = runBlocking {
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

    private fun equal(c1: Conversation?, c2: Conversation?): Boolean {
        return c1 != null && c2 != null && c1.deviceAddress == c2.deviceAddress &&
                c1.deviceName == c2.deviceName && c1.displayName == c2.displayName && c1.color == c2.color
    }
}
