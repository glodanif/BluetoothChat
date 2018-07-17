package com.glodanif.bluetoothchat.di.module

import com.glodanif.bluetoothchat.data.model.MessagesStorage
import com.glodanif.bluetoothchat.di.PerComponent
import com.glodanif.bluetoothchat.ui.activity.ImagePreviewActivity
import com.glodanif.bluetoothchat.ui.presenter.ImagePreviewPresenter
import dagger.Module
import dagger.Provides
import java.io.File

@Module
class ImagePreviewModule(private val messageId: Long, private val image: File, private val activity: ImagePreviewActivity) {

    @Provides
    @PerComponent
    internal fun providePresenter(messages: MessagesStorage): ImagePreviewPresenter = ImagePreviewPresenter(messageId, image, activity, messages)
}
