package com.glodanif.bluetoothchat.ui.view

import com.glodanif.bluetoothchat.ui.viewmodel.ContactViewModel

interface ContactChooserView {
    fun showContacts(contacts: List<ContactViewModel>)
    fun showNoContacts()
}
