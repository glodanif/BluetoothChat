package com.glodanif.bluetoothchat.di.module

import com.glodanif.bluetoothchat.data.model.*
import com.glodanif.bluetoothchat.di.PerActivity
import com.glodanif.bluetoothchat.ui.activity.ChatActivity
import com.glodanif.bluetoothchat.ui.activity.ConversationsActivity
import com.glodanif.bluetoothchat.ui.activity.ProfileActivity
import com.glodanif.bluetoothchat.ui.activity.ReceivedImagesActivity
import com.glodanif.bluetoothchat.ui.presenter.ChatPresenter
import com.glodanif.bluetoothchat.ui.presenter.ConversationsPresenter
import com.glodanif.bluetoothchat.ui.presenter.ProfilePresenter
import com.glodanif.bluetoothchat.ui.presenter.ReceivedImagesPresenter
import dagger.Module
import dagger.Provides

@Module
class ReceivedImagesModule(private val address: String?, private val activity: ReceivedImagesActivity) {

    @Provides
    @PerActivity
    internal fun providePresenter(messages: MessagesStorage): ReceivedImagesPresenter = ReceivedImagesPresenter(address, activity, messages)
}
