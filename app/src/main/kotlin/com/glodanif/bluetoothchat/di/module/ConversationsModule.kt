package com.glodanif.bluetoothchat.di.module

import com.glodanif.bluetoothchat.data.model.BluetoothConnector
import com.glodanif.bluetoothchat.data.model.ConversationsStorage
import com.glodanif.bluetoothchat.data.model.MessagesStorage
import com.glodanif.bluetoothchat.data.model.ProfileManager
import com.glodanif.bluetoothchat.di.PerComponent
import com.glodanif.bluetoothchat.ui.activity.ConversationsActivity
import com.glodanif.bluetoothchat.ui.presenter.ConversationsPresenter
import com.glodanif.bluetoothchat.ui.viewmodel.converter.ConversationConverter
import dagger.Module
import dagger.Provides

@Module
class ConversationsModule(private val activity: ConversationsActivity) {

    @Provides
    @PerComponent
    internal fun providePresenter(connector: BluetoothConnector, conversationStorage: ConversationsStorage, messageStorage: MessagesStorage,
                                  profileManager: ProfileManager, converter: ConversationConverter): ConversationsPresenter =
            ConversationsPresenter(activity, connector, conversationStorage, messageStorage, profileManager, converter)
}
