package com.glodanif.bluetoothchat.datasource

import android.graphics.Color
import androidx.test.InstrumentationRegistry
import androidx.test.runner.AndroidJUnit4
import com.glodanif.bluetoothchat.data.model.UserPreferences
import com.glodanif.bluetoothchat.data.model.UserPreferencesStorageImpl
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UserPreferencesInstrumentedTest {

    private lateinit var storage: UserPreferences

    @Before
    fun prepare() {
        val context = InstrumentationRegistry.getTargetContext()
        storage = UserPreferencesStorageImpl(context).apply {
            saveNewSoundPreference(false)
            saveNewClassificationPreference(true)
            saveChatBgColor(Color.RED)
        }
    }

    @After
    fun release() = with(storage) {
        saveNewSoundPreference(false)
        saveNewClassificationPreference(true)
        saveChatBgColor(0)
    }

    @Test
    fun color() {
        storage.saveChatBgColor(Color.GREEN)
        val savedColor = storage.getChatBackgroundColor()
        assertTrue(Color.GREEN == savedColor)
    }

    @Test
    fun sound() {
        storage.saveNewSoundPreference(true)
        val savedSound = storage.isSoundEnabled()
        assertTrue(savedSound)
    }

    @Test
    fun classification() {
        storage.saveNewClassificationPreference(false)
        val savedClassification = storage.isClassificationEnabled()
        assertTrue(!savedClassification)
    }
}
