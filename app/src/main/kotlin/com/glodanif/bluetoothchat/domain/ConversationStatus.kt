package com.glodanif.bluetoothchat.domain

sealed class ConversationStatus {
    object NotConnected : ConversationStatus()
    object Connected : ConversationStatus()
    object Pending : ConversationStatus()
    class ConnectedToOther(val displayName: String, val deviceName: String) : ConversationStatus()
    class IncomingRequest(val displayName: String, val deviceName: String) : ConversationStatus()
}