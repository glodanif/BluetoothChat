package com.glodanif.bluetoothchat

import com.glodanif.bluetoothchat.data.model.SettingsManager
import com.glodanif.bluetoothchat.ui.presenter.ProfilePresenter
import com.glodanif.bluetoothchat.ui.view.ProfileView
import com.nhaarman.mockito_kotlin.verify
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule

class ProfilePresenterUnitTest {

    @JvmField
    @Rule
    val mockitoRule: MockitoRule = MockitoJUnit.rule()

    @Mock
    private lateinit var model: SettingsManager
    @Mock
    private lateinit var view: ProfileView

    private lateinit var presenter: ProfilePresenter

    @Before
    fun setup() {
        presenter = ProfilePresenter(view, model)
    }

    @Test
    fun validation_emptyUserName() {
        presenter.onNameChanged("")
        presenter.saveUser()
        verify(view).showNotValidNameError()
    }

    @Test
    fun validation_forbiddenCharacters() {
        presenter.onNameChanged("Test#")
        presenter.saveUser()
        verify(view).showNotValidNameError()
    }

    @Test
    fun validation_longUserName() {
        presenter.onNameChanged("Test longer that 25 character")
        presenter.saveUser()
        verify(view).showNotValidNameError()
    }

    @Test
    fun validation_validUsername() {
        presenter.onNameChanged("Test")
        presenter.saveUser()
        verify(view).redirectToConversations()
    }

    @Test
    fun input_typedText() {
        presenter.onNameChanged("Test")
        verify(view).showUserData("Test", 0)
    }

    @Test
    fun color_preparePicker() {
        presenter.prepareColorPicker()
        verify(view).showColorPicker(0)
    }

    @Test
    fun color_onColorPicked() {
        presenter.onColorPicked(0)
        verify(view).showUserData(uninitialized(), 0)
    }

    @Test
    fun onStart_displayProfile() {
        presenter.loadSavedUser()
        verify(view).prefillUsername(uninitialized())
        verify(view).showUserData(uninitialized(), 0)
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T> uninitialized(): T = null as T
}
