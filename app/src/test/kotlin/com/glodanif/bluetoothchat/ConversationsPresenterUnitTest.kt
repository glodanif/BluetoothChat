package com.glodanif.bluetoothchat

import com.glodanif.bluetoothchat.model.BluetoothConnector
import com.glodanif.bluetoothchat.model.ConversationsStorage
import com.glodanif.bluetoothchat.model.SettingsManager
import com.glodanif.bluetoothchat.presenter.ConversationsPresenter
import com.glodanif.bluetoothchat.presenter.ProfilePresenter
import com.glodanif.bluetoothchat.view.ConversationsView
import com.glodanif.bluetoothchat.view.ProfileView
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations

class ConversationsPresenterUnitTest {

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
        MockitoAnnotations.initMocks(this)
        presenter = ConversationsPresenter(view, connectionModel, storageModel, settingsModel)
    }

    @Test
    fun connection_reject() {
        presenter.rejectConnection()
        Mockito.verify(view).hideActions()
    }

    @Test
    fun user_load() {
        presenter.loadUserProfile()
        Mockito.verify(view).showUserProfile(uninitialized(), 0)
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T> uninitialized(): T = null as T
}
