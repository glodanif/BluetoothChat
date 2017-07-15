package com.glodanif.bluetoothchat

import com.glodanif.bluetoothchat.model.BluetoothConnector
import com.glodanif.bluetoothchat.model.ConversationsStorage
import com.glodanif.bluetoothchat.model.SettingsManager
import com.glodanif.bluetoothchat.presenter.ConversationsPresenter
import com.glodanif.bluetoothchat.presenter.ProfilePresenter
import com.glodanif.bluetoothchat.view.ConversationsView
import com.glodanif.bluetoothchat.view.ProfileView
import com.nhaarman.mockito_kotlin.verify
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule

class ConversationsPresenterUnitTest {

    @JvmField
    @Rule
    val mockitoRule: MockitoRule = MockitoJUnit.rule()

    @Mock
    lateinit var settingsModel: SettingsManager
    @Mock
    lateinit var storageModel: ConversationsStorage
    @Mock
    lateinit var connectionModel: BluetoothConnector
    @Mock
    lateinit var view: ConversationsView

    lateinit var presenter: ConversationsPresenter

    @Before
    fun setup() {
        presenter = ConversationsPresenter(view, connectionModel, storageModel, settingsModel)
    }

    @Test
    fun connection_reject() {
        presenter.rejectConnection()
        verify(view).hideActions()
    }

    @Test
    fun user_load() {
        presenter.loadUserProfile()
        verify(view).showUserProfile(uninitialized(), 0)
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T> uninitialized(): T = null as T
}
