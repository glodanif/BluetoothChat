package com.glodanif.bluetoothchat.presenter

import com.glodanif.bluetoothchat.data.model.*
import com.glodanif.bluetoothchat.ui.presenter.ChatPresenter
import com.glodanif.bluetoothchat.ui.view.ChatView
import com.glodanif.bluetoothchat.ui.viewmodel.converter.ChatMessageConverter
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.verify
import kotlinx.coroutines.experimental.Dispatchers
import org.junit.Before
import org.junit.Test

class ChatPresenterUnitTest {

    private val address = "00:00:00:00:00:00"
    @RelaxedMockK
    private lateinit var view: ChatView
    @RelaxedMockK
    private lateinit var conversationStorage: ConversationsStorage
    @RelaxedMockK
    private lateinit var messageStorage: MessagesStorage
    @RelaxedMockK
    private lateinit var scanner: BluetoothScanner
    @RelaxedMockK
    private lateinit var connector: BluetoothConnector
    @RelaxedMockK
    private lateinit var converter: ChatMessageConverter
    @RelaxedMockK
    private lateinit var preferences: UserPreferences

    private lateinit var presenter: ChatPresenter

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        presenter = ChatPresenter(address, view, conversationStorage, messageStorage,
                scanner, connector, preferences, converter, Dispatchers.Unconfined, Dispatchers.Unconfined)
    }

    @Test
    fun disconnect() {
        presenter.disconnect()
        verify { view.showStatusNotConnected() }
        verify { view.showNotConnectedToAnyDevice() }
    }
}
