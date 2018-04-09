package com.glodanif.bluetoothchat.ui.viewmodel.converter

import android.content.Context
import com.glodanif.bluetoothchat.R
import com.glodanif.bluetoothchat.data.entity.Conversation
import com.glodanif.bluetoothchat.data.entity.MessageType
import com.glodanif.bluetoothchat.utils.getRelativeTime
import com.glodanif.bluetoothchat.ui.viewmodel.ConversationViewModel

class ConversationConverter(private val context: Context) {

    fun transform(conversation: Conversation): ConversationViewModel {

        val lastMessage = when {
            conversation.messageType == MessageType.IMAGE -> context.getString(R.string.chat__image_message, "\uD83D\uDCCE")
            !conversation.lastMessage.isNullOrEmpty() -> conversation.lastMessage
            else -> null
        }

        val lastActivity = if (!conversation.lastMessage.isNullOrEmpty() || conversation.messageType == MessageType.IMAGE) {
            conversation.lastActivity?.getRelativeTime(context)
        } else {
            null
        }

        return ConversationViewModel(
                conversation.deviceAddress,
                conversation.deviceName,
                conversation.displayName,
                "${conversation.displayName} (${conversation.deviceName})",
                conversation.color,
                lastMessage,
                conversation.lastActivity,
                lastActivity,
                conversation.notSeen

        )
    }

    fun transform(conversationCollection: Collection<Conversation>): List<ConversationViewModel> {
        return conversationCollection.map {
            transform(it)
        }
    }
}
