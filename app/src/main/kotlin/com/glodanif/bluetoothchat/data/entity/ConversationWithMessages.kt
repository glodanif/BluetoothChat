package com.glodanif.bluetoothchat.data.entity

import androidx.room.Relation
import kotlin.collections.ArrayList

data class ConversationWithMessages(
        var address: String,
        var deviceName: String,
        var displayName: String,
        var color: Int
) {

    @Relation(parentColumn = "address", entityColumn = "deviceAddress", entity = ChatMessage::class)
    var messages: List<SimpleChatMessage> = ArrayList()
}
