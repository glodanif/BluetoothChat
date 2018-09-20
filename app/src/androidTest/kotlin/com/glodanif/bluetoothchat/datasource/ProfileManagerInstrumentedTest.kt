package com.glodanif.bluetoothchat.datasource

import android.graphics.Color
import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import com.glodanif.bluetoothchat.data.model.ProfileManager
import com.glodanif.bluetoothchat.data.model.ProfileManagerImpl
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProfileManagerInstrumentedTest {

    private lateinit var storage: ProfileManager

    @Before
    fun prepare() {
        val context = InstrumentationRegistry.getTargetContext()
        storage = ProfileManagerImpl(context).apply {
            saveUserName("A")
            saveUserColor(Color.RED)
        }
    }

    @After
    fun release() = with(storage) {
        saveUserName("")
        saveUserColor(0)
    }

    @Test
    fun color() {
        storage.saveUserColor(Color.GREEN)
        val savedColor = storage.getUserColor()
        assertTrue(Color.GREEN == savedColor)
    }

    @Test
    fun name() {
        storage.saveUserName("B")
        val savedName = storage.getUserName()
        assertTrue(savedName == "B")
    }

    @Test
    fun initialized() {
        storage.saveUserName("B")
        assertTrue(storage.isInitialized())
    }

    @Test
    fun notInitialized() {
        storage.saveUserName("")
        assertFalse(storage.isInitialized())
    }
}
