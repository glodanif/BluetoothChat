package com.glodanif.bluetoothchat.di.module

import com.glodanif.bluetoothchat.data.entity.ChatMessage
import com.glodanif.bluetoothchat.data.model.*
import com.glodanif.bluetoothchat.di.PerActivity
import com.glodanif.bluetoothchat.ui.activity.*
import com.glodanif.bluetoothchat.ui.presenter.*
import dagger.Module
import dagger.Provides

@Module
class ImagePreviewModule(private val message: ChatMessage, private val activity: ImagePreviewActivity) {

    @Provides
    @PerActivity
    internal fun providePresenter(messages: MessagesStorage): ImagePreviewPresenter = ImagePreviewPresenter(message, activity, messages)
}
