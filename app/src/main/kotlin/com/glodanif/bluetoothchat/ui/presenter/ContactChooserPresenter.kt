package com.glodanif.bluetoothchat.ui.presenter

import com.glodanif.bluetoothchat.ui.viewmodel.converter.ContactConverter
import com.glodanif.bluetoothchat.data.model.ConversationsStorage
import com.glodanif.bluetoothchat.ui.view.ContactChooserView

class ContactChooserPresenter(private val view: ContactChooserView, private val model: ConversationsStorage) {

    private val converter = ContactConverter()

    fun loadContacts() {
        model.getConversations {
            if (!it.isEmpty()) {
                view.showContacts(converter.transform(it))
            } else {
                view.showNoContacts()
            }
        }
    }
}
