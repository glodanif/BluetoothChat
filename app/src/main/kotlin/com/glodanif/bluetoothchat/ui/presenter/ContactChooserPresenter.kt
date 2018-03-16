package com.glodanif.bluetoothchat.ui.presenter

import android.os.Handler
import com.glodanif.bluetoothchat.ui.viewmodel.converter.ContactConverter
import com.glodanif.bluetoothchat.data.model.ConversationsStorage
import com.glodanif.bluetoothchat.di.ComponentsManager
import com.glodanif.bluetoothchat.ui.view.ContactChooserView
import kotlinx.coroutines.experimental.launch
import javax.inject.Inject

class ContactChooserPresenter(private val view: ContactChooserView) {

    private val converter = ContactConverter()

    @Inject
    lateinit var model: ConversationsStorage

    private val handler = Handler()

    init {
        ComponentsManager.getDataSourceComponent().inject(this)
    }

    fun loadContacts() {

        launch {
            val contacts = model.getConversations()

            handler.post {
                if (!contacts.isEmpty()) {
                    view.showContacts(converter.transform(contacts))
                } else {
                    view.showNoContacts()
                }
            }
        }
    }
}
