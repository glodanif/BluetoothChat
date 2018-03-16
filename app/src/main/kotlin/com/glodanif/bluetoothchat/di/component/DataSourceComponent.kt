package com.glodanif.bluetoothchat.di.component

import com.glodanif.bluetoothchat.di.module.DataSourceModule
import com.glodanif.bluetoothchat.ui.presenter.*
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = arrayOf(DataSourceModule::class))
interface DataSourceComponent {
    fun inject(presenter: ConversationsPresenter)
    fun inject(presenter: ChatPresenter)
    fun inject(presenter: ProfilePresenter)
    fun inject(presenter: ContactChooserPresenter)
    fun inject(presenter: ImagePreviewPresenter)
    fun inject(presenter: ReceivedImagesPresenter)
}
