package com.glodanif.bluetoothchat.presenter

import com.glodanif.bluetoothchat.data.entity.ConversationWithMessages
import com.glodanif.bluetoothchat.data.model.BluetoothConnector
import com.glodanif.bluetoothchat.data.model.ConversationsStorage
import com.glodanif.bluetoothchat.data.model.MessagesStorage
import com.glodanif.bluetoothchat.data.model.ProfileManager
import com.glodanif.bluetoothchat.ui.presenter.ConversationsPresenter
import com.glodanif.bluetoothchat.ui.view.ConversationsView
import com.glodanif.bluetoothchat.ui.viewmodel.ConversationViewModel
import com.glodanif.bluetoothchat.ui.viewmodel.converter.ConversationConverter
import io.mockk.*
import io.mockk.impl.annotations.RelaxedMockK
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test

class ConversationsPresenterUnitTest {

    @RelaxedMockK
    private lateinit var profile: ProfileManager
    @RelaxedMockK
    private lateinit var conversationsStorage: ConversationsStorage
    @RelaxedMockK
    private lateinit var messageStorage: MessagesStorage
    @RelaxedMockK
    private lateinit var connector: BluetoothConnector
    @RelaxedMockK
    private lateinit var view: ConversationsView
    @RelaxedMockK
    private lateinit var converter: ConversationConverter

    private lateinit var presenter: ConversationsPresenter

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        presenter = ConversationsPresenter(view, connector, conversationsStorage,
                messageStorage, profile, converter, Dispatchers.Unconfined, Dispatchers.Unconfined)
    }

    @Test
    fun loadConversations_empty() = runBlocking {
        coEvery { conversationsStorage.getConversations() } returns ArrayList()
        presenter.loadConversations()
        verify { view.showNoConversations() }
    }

    @Test
    fun loadConversations_notEmpty_notConnected() {
        val list = listOf<ConversationWithMessages>(mockk())
        val viewModels = arrayListOf<ConversationViewModel>(mockk())
        coEvery { conversationsStorage.getConversations() } returns list
        every { converter.transform(list) } returns viewModels
        every { connector.isConnected() } returns false
        presenter.loadConversations()
        verify { view.showConversations(viewModels, null) }
    }

    @Test
    fun loadConversations_notEmpty_connected() {
        val address = "00:00:00:00"
        val list = listOf<ConversationWithMessages>(mockk())
        val viewModels = arrayListOf<ConversationViewModel>(mockk())
        coEvery { conversationsStorage.getConversations() } returns list
        every { converter.transform(list) } returns viewModels
        every { connector.isConnected() } returns true
        every { connector.getCurrentConversation()?.deviceAddress } returns address
        presenter.loadConversations()
        verify { view.showConversations(viewModels, address) }
    }

    @Test
    fun startChat() {
        val conversation = mockk<ConversationViewModel>()
        presenter.startChat(conversation)
        verify { view.hideActions() }
        verify { view.redirectToChat(conversation) }
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
