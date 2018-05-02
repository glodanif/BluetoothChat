package com.glodanif.bluetoothchat.presenter

import com.glodanif.bluetoothchat.data.model.BluetoothConnector
import com.glodanif.bluetoothchat.data.model.ConversationsStorage
import com.glodanif.bluetoothchat.data.model.SettingsManager
import com.glodanif.bluetoothchat.ui.presenter.ConversationsPresenter
import com.glodanif.bluetoothchat.ui.view.ConversationsView
import com.glodanif.bluetoothchat.ui.viewmodel.converter.ConversationConverter
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.verify
import org.junit.Before
import org.junit.Test

class ConversationsPresenterUnitTest {

    @RelaxedMockK
    lateinit var settings: SettingsManager
    @RelaxedMockK
    lateinit var storage: ConversationsStorage
    @RelaxedMockK
    lateinit var connector: BluetoothConnector
    @RelaxedMockK
    lateinit var view: ConversationsView
    @RelaxedMockK
    lateinit var converter: ConversationConverter

    lateinit var presenter: ConversationsPresenter

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        presenter = ConversationsPresenter(view, connector, storage, settings, converter)
    }

    @Test
    fun connection_reject() {
        presenter.rejectConnection()
        verify { view.hideActions() }
    }

    @Test
    fun connection_prepare() {
        presenter.prepareConnection()
        verify { view.dismissConversationNotification() }
    }

    @Test
    fun user_load() {
        presenter.loadUserProfile()
        verify { view.showUserProfile(any(), 0) }
    }
}
