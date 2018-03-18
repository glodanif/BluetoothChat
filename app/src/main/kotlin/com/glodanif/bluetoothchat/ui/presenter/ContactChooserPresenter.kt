package com.glodanif.bluetoothchat.ui.presenter

import com.glodanif.bluetoothchat.data.model.ConversationsStorage
import com.glodanif.bluetoothchat.di.ComponentsManager
import com.glodanif.bluetoothchat.ui.view.ContactChooserView
import com.glodanif.bluetoothchat.ui.viewmodel.converter.ContactConverter
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch
import javax.inject.Inject

class ContactChooserPresenter(private val view: ContactChooserView, private val model: ConversationsStorage) {

    private val converter = ContactConverter()

    fun loadContacts() {

        launch(UI) {
            val contacts = async(CommonPool) { model.getConversations() }.await()
            if (!contacts.isEmpty()) {
                view.showContacts(converter.transform(contacts))
            } else {
                view.showNoContacts()
            }
        }
    }
}
