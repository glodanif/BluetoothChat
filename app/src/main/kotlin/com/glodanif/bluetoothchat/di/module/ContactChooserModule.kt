package com.glodanif.bluetoothchat.di.module

import com.glodanif.bluetoothchat.data.model.ConversationsStorage
import com.glodanif.bluetoothchat.di.PerComponent
import com.glodanif.bluetoothchat.ui.activity.ContactChooserActivity
import com.glodanif.bluetoothchat.ui.presenter.ContactChooserPresenter
import com.glodanif.bluetoothchat.ui.viewmodel.converter.ContactConverter
import dagger.Module
import dagger.Provides

@Module
class ContactChooserModule(private val activity: ContactChooserActivity) {

    @Provides
    @PerComponent
    internal fun providePresenter(storage: ConversationsStorage, converter: ContactConverter): ContactChooserPresenter = ContactChooserPresenter(activity, storage, converter)
}
