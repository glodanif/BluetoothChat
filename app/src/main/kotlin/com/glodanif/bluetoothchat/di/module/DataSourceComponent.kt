package com.glodanif.bluetoothchat.di.module

import com.glodanif.bluetoothchat.ui.presenter.ChatPresenter
import com.glodanif.bluetoothchat.ui.presenter.ConversationsPresenter
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules= arrayOf(DataSourceModule::class))
interface DataSourceComponent {
    fun inject(presenter: ConversationsPresenter)
    fun inject(presenter: ChatPresenter)
}