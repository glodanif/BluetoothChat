package com.glodanif.bluetoothchat.ui.presenter

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.OnLifecycleEvent
import com.glodanif.bluetoothchat.data.model.ConversationsStorage
import com.glodanif.bluetoothchat.ui.view.ContactChooserView
import com.glodanif.bluetoothchat.ui.viewmodel.converter.ContactConverter
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch
import kotlin.coroutines.experimental.CoroutineContext

class ContactChooserPresenter(private val view: ContactChooserView,
                              private val model: ConversationsStorage,
                              private val converter: ContactConverter,
                              private val uiContext: CoroutineContext = UI,
                              private val bgContext: CoroutineContext = CommonPool): LifecycleObserver {

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun loadContacts() = launch(uiContext) {

        val contacts = async(bgContext) { model.getContacts() }.await()

        if (contacts.isNotEmpty()) {
            val viewModels = converter.transform(contacts)
            view.showContacts(viewModels)
        } else {
            view.showNoContacts()
        }
    }
}
