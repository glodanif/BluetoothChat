package com.glodanif.bluetoothchat.ui.viewmodel.converter

import android.content.Context
import com.glodanif.bluetoothchat.R
import com.glodanif.bluetoothchat.data.entity.Conversation
import com.glodanif.bluetoothchat.data.entity.ConversationWithMessages
import com.glodanif.bluetoothchat.data.service.message.PayloadType
import com.glodanif.bluetoothchat.utils.getRelativeTime
import com.glodanif.bluetoothchat.ui.viewmodel.ConversationViewModel
import java.util.*

class ConversationConverter(private val context: Context) {

    fun transform(conversation: ConversationWithMessages): ConversationViewModel {

        val lastMessage = conversation.messages.asSequence()
                .sortedByDescending { it.date }
                .firstOrNull()

        val notSeen = conversation.messages.filterNot { it.seenHere }.size

        val lastMessageText = when {
            lastMessage?.messageType == PayloadType.IMAGE ->
                context.getString(R.string.chat__image_message, "\uD83D\uDCCE")
            !lastMessage?.text.isNullOrEmpty() ->
                lastMessage?.text
            else -> null
        }

        val lastActivity = if (!lastMessage?.text.isNullOrEmpty() || lastMessage?.messageType == PayloadType.IMAGE) {
            lastMessage?.date?.getRelativeTime(context)
        } else {
            null
        }

        return ConversationViewModel(
                conversation.address,
                conversation.deviceName,
                conversation.displayName,
                "${conversation.displayName} (${conversation.deviceName})",
                conversation.color,
                lastMessageText,
                lastMessage?.date,
                lastActivity,
                notSeen

        )
    }

    fun transform(conversationCollection: Collection<ConversationWithMessages>): List<ConversationViewModel> {
        return conversationCollection.map {
            transform(it)
        }
    }

    fun transform(conversation: Conversation): ConversationViewModel {

        return ConversationViewModel(
                conversation.deviceAddress,
                conversation.deviceName,
                conversation.displayName,
                "${conversation.displayName} (${conversation.deviceName})",
                conversation.color,
                null,
                Date(),
                null,
                0
        )
    }
}
