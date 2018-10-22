package com.glodanif.bluetoothchat.ui.presenter

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.glodanif.bluetoothchat.domain.interactor.GetContactsInteractor
import com.glodanif.bluetoothchat.ui.router.ContactChooserRouter
import com.glodanif.bluetoothchat.ui.view.ContactChooserView
import com.glodanif.bluetoothchat.ui.viewmodel.converter.ContactConverter

class ContactChooserPresenter(private val view: ContactChooserView,
                              private val router: ContactChooserRouter,
                              private val getContactsInteractor: GetContactsInteractor,
                              private val converter: ContactConverter
) : LifecycleObserver {

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun loadContacts() {

        getContactsInteractor.execute(Unit,
                onResult = { contacts ->
                    if (contacts.isNotEmpty()) {
                        view.showContacts(converter.transform(contacts))
                    } else {
                        view.showNoContacts()
                    }
                }
        )
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun stop() {
        getContactsInteractor.cancel()
    }

    fun onChoseContact(address: String) {
        router.redirectToChat(address)
    }
}
