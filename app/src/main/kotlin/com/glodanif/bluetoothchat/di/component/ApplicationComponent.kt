package com.glodanif.bluetoothchat.di.component

import com.glodanif.bluetoothchat.data.model.ConversationsStorage
import com.glodanif.bluetoothchat.data.model.MessagesStorage
import com.glodanif.bluetoothchat.data.model.SettingsManager
import com.glodanif.bluetoothchat.di.module.ApplicationModule
import com.glodanif.bluetoothchat.ui.presenter.*
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = arrayOf(ApplicationModule::class))
interface ApplicationComponent {

    fun inject(presenter: ContactChooserPresenter)
    fun inject(presenter: ImagePreviewPresenter)
    fun inject(presenter: ReceivedImagesPresenter)

    fun conversationsStorage(): ConversationsStorage
    fun messagesStorage(): MessagesStorage
    fun settingsManager(): SettingsManager
}
