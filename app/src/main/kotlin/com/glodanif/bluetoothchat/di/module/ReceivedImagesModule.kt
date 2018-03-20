package com.glodanif.bluetoothchat.di.module

import com.glodanif.bluetoothchat.data.model.MessagesStorage
import com.glodanif.bluetoothchat.di.PerActivity
import com.glodanif.bluetoothchat.ui.activity.ReceivedImagesActivity
import com.glodanif.bluetoothchat.ui.presenter.ReceivedImagesPresenter
import dagger.Module
import dagger.Provides

@Module
class ReceivedImagesModule(private val address: String?, private val activity: ReceivedImagesActivity) {

    @Provides
    @PerActivity
    internal fun providePresenter(messages: MessagesStorage): ReceivedImagesPresenter = ReceivedImagesPresenter(address, activity, messages)
}
