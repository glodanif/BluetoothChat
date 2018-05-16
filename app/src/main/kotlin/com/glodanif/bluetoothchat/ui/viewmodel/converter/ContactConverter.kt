package com.glodanif.bluetoothchat.ui.viewmodel.converter

import com.amulyakhare.textdrawable.TextDrawable
import com.glodanif.bluetoothchat.data.entity.Conversation
import com.glodanif.bluetoothchat.ui.viewmodel.ContactViewModel
import com.glodanif.bluetoothchat.utils.getFirstLetter

class ContactConverter {

    fun transform(conversation: Conversation): ContactViewModel {
        return ContactViewModel(
                conversation.deviceAddress,
                "${conversation.displayName} (${conversation.deviceName})",
                TextDrawable.builder()
                        .buildRound(conversation.displayName.getFirstLetter(), conversation.color)
        )
    }

    fun transform(conversationCollection: Collection<Conversation>): List<ContactViewModel> {
        return conversationCollection.map {
            transform(it)
        }
    }
}
