package com.glodanif.bluetoothchat.presenter

import com.glodanif.bluetoothchat.data.model.SettingsManager
import com.glodanif.bluetoothchat.ui.presenter.ProfilePresenter
import com.glodanif.bluetoothchat.ui.view.ProfileView
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.verify
import org.junit.Before
import org.junit.Test

class ProfilePresenterUnitTest {

    @RelaxedMockK
    private lateinit var model: SettingsManager
    @RelaxedMockK
    private lateinit var view: ProfileView

    private lateinit var presenter: ProfilePresenter

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        presenter = ProfilePresenter(view, model)
    }

    @Test
    fun validation_emptyUserName() {
        presenter.onNameChanged("")
        presenter.saveUser()
        verify { view.showNotValidNameError() }
    }

    @Test
    fun validation_forbiddenCharacters() {
        presenter.onNameChanged("Text#")
        presenter.saveUser()
        verify { view.showNotValidNameError() }
    }

    @Test
    fun validation_longUserName() {
        presenter.onNameChanged("Text longer that 25 character")
        presenter.saveUser()
        verify { view.showNotValidNameError() }
    }

    @Test
    fun validation_validUsername() {
        presenter.onNameChanged("Text")
        presenter.saveUser()
        verify { view.redirectToConversations() }
    }

    @Test
    fun input_typedText() {
        presenter.onNameChanged("Text")
        verify { view.showUserData("Text", 0) }
    }

    @Test
    fun color_preparePicker() {
        presenter.prepareColorPicker()
        verify { view.showColorPicker(0) }
    }

    @Test
    fun color_onColorPicked() {
        presenter.onColorPicked(0)
        verify { view.showUserData(any(), 0) }
    }

    @Test
    fun onStart_displayProfile() {
        presenter.loadSavedUser()
        verify { view.prefillUsername(any())}
        verify { view.showUserData(any(), 0)}
    }
}
