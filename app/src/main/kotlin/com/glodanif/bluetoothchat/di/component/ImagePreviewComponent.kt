package com.glodanif.bluetoothchat.di.component

import com.glodanif.bluetoothchat.di.PerActivity
import com.glodanif.bluetoothchat.di.module.*
import com.glodanif.bluetoothchat.ui.activity.*
import dagger.Component

@PerActivity
@Component(dependencies = arrayOf(ApplicationComponent::class), modules = arrayOf(ImagePreviewModule::class))
interface ImagePreviewComponent {
    fun inject(activity: ImagePreviewActivity)
}
