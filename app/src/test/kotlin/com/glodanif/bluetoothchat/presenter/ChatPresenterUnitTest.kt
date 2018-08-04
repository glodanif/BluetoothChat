package com.glodanif.bluetoothchat.presenter

import com.glodanif.bluetoothchat.data.model.*
import com.glodanif.bluetoothchat.ui.presenter.ChatPresenter
import com.glodanif.bluetoothchat.ui.view.ChatView
import com.glodanif.bluetoothchat.ui.viewmodel.converter.ChatMessageConverter
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.RelaxedMockK
import org.junit.Before
import kotlin.coroutines.experimental.EmptyCoroutineContext

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
                scanner, connector, preferences, converter, EmptyCoroutineContext, EmptyCoroutineContext)
    }

    /*@Test
    fun loading_empty() {
        coEvery { storage.getConversations() } returns ArrayList()
        presenter.loadContacts()
        verify { view.showNoContacts() }
    }

    @Test
    fun loading_notEmpty() {
        val list = arrayListOf<Conversation>(mockk())
        val viewModels = arrayListOf<ContactViewModel>(mockk())
        coEvery { storage.getConversations() } returns list
        every { converter.transform(list) } returns viewModels
        presenter.loadContacts()
        verify { view.showContacts(viewModels) }
    }*/
}
