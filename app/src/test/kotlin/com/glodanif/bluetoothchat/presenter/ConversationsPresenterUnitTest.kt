package com.glodanif.bluetoothchat.presenter

import com.glodanif.bluetoothchat.data.model.BluetoothConnector
import com.glodanif.bluetoothchat.data.model.ConversationsStorage
import com.glodanif.bluetoothchat.data.model.MessagesStorage
import com.glodanif.bluetoothchat.data.model.ProfileManager
import com.glodanif.bluetoothchat.ui.presenter.ConversationsPresenter
import com.glodanif.bluetoothchat.ui.view.ConversationsView
import com.glodanif.bluetoothchat.ui.viewmodel.converter.ConversationConverter
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.verify
import org.junit.Before
import org.junit.Test
import kotlin.coroutines.experimental.EmptyCoroutineContext

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
        presenter = ConversationsPresenter(view, connector, conversationsStorage, messageStorage, profile, converter, EmptyCoroutineContext, EmptyCoroutineContext)
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
