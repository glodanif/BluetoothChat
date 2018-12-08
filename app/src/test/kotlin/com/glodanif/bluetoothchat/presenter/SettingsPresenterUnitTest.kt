package com.glodanif.bluetoothchat.presenter

import android.graphics.Color
import com.glodanif.bluetoothchat.data.model.UserPreferences
import com.glodanif.bluetoothchat.ui.presenter.SettingsPresenter
import com.glodanif.bluetoothchat.ui.view.SettingsView
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import org.junit.Before
import org.junit.Test

class SettingsPresenterUnitTest {

    @RelaxedMockK
    private lateinit var view: SettingsView
    @RelaxedMockK
    private lateinit var preferences: UserPreferences

    private lateinit var presenter: SettingsPresenter

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        presenter = SettingsPresenter(view, preferences, Dispatchers.Unconfined, Dispatchers.Unconfined)
    }

    @Test
    fun loading() {
        every { preferences.isSoundEnabled() } returns true
        every { preferences.isClassificationEnabled() } returns true
        every { preferences.getChatBackgroundColor() } returns Color.GREEN
        presenter.loadPreferences()
        verify { view.displayNotificationSetting(true) }
        verify { view.displayDiscoverySetting(true) }
        verify { view.displayAppearanceSettings(Color.GREEN) }
    }

    @Test
    fun prepare_colorPicker() {
        every { preferences.getChatBackgroundColor() } returns Color.GREEN
        presenter.prepareColorPicker()
        verify { view.displayColorPicker(Color.GREEN) }
    }

    @Test
    fun newColor() {
        presenter.onNewColorPicked(Color.GREEN)
        verify { view.displayAppearanceSettings(Color.GREEN) }
    }
}
