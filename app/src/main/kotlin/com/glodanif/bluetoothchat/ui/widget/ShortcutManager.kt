package com.glodanif.bluetoothchat.ui.widget

import androidx.annotation.ColorInt

interface ShortcutManager {

    fun addSearchShortcut()
    fun addConversationShortcut(address: String, name: String, @ColorInt color: Int)
    fun requestPinConversationShortcut(address: String, name: String, @ColorInt color: Int)
    fun removeConversationShortcut(address: String)
    fun isRequestPinShortcutSupported(): Boolean
}
