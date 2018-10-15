package com.glodanif.bluetoothchat.ui.widget

import android.content.Context
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.graphics.drawable.Icon
import android.net.Uri
import android.os.Build
import androidx.annotation.ColorInt
import androidx.annotation.RequiresApi
import com.amulyakhare.textdrawable.TextDrawable
import com.glodanif.bluetoothchat.R
import com.glodanif.bluetoothchat.ui.activity.ChatActivity
import com.glodanif.bluetoothchat.ui.activity.ConversationsActivity
import com.glodanif.bluetoothchat.ui.activity.ScanActivity
import com.glodanif.bluetoothchat.utils.getBitmap
import com.glodanif.bluetoothchat.utils.getFirstLetter
import java.util.*

class ShortcutManagerImpl(private val context: Context) : ShortcutManager {

    private val idSearch = "id.search"

    private var shortcutManager: android.content.pm.ShortcutManager? = null

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            shortcutManager = context
                    .getSystemService(android.content.pm.ShortcutManager::class.java)
        }
    }

    override fun addSearchShortcut() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {

            shortcutManager?.let { manager ->

                val isSearchAdded = manager.dynamicShortcuts.asSequence()
                        .filter { it.id == idSearch }
                        .any()

                if (isSearchAdded) {
                    return
                }
            }

            val shortcut = ShortcutInfo.Builder(context, idSearch)
                    .setShortLabel(context.getString(R.string.scan__scan))
                    .setLongLabel(context.getString(R.string.scan__scan))
                    .setIcon(Icon.createWithResource(context, R.drawable.ic_search_black_24dp))
                    .setIntents(arrayOf(
                            Intent(Intent.ACTION_MAIN, Uri.EMPTY, context, ConversationsActivity::class.java)
                                    .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK),
                            Intent(Intent.ACTION_SEARCH, Uri.EMPTY, context, ScanActivity::class.java)

                    ))
                    .build()

            shortcutManager?.addDynamicShortcuts(Arrays.asList(shortcut))
        }
    }

    override fun addConversationShortcut(address: String, name: String, @ColorInt color: Int) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {

            removeLatestIfNeeded(address)

            val shortcut = createConversationShortcut(address, name, color)
            shortcutManager?.addDynamicShortcuts(Arrays.asList(shortcut))
        }
    }

    override fun removeConversationShortcut(address: String) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            shortcutManager?.removeDynamicShortcuts(Arrays.asList(address))
        }
    }

    override fun requestPinConversationShortcut(address: String, name: String, color: Int) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val shortcut = createConversationShortcut(address, name, color)
            shortcutManager?.requestPinShortcut(shortcut, null)
        }
    }

    override fun isRequestPinShortcutSupported() =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                shortcutManager?.isRequestPinShortcutSupported ?: false
            } else {
                false
            }

    @RequiresApi(Build.VERSION_CODES.N_MR1)
    private fun createConversationShortcut(address: String, name: String, @ColorInt color: Int): ShortcutInfo {

        val drawable = TextDrawable.builder().buildRound(name.getFirstLetter(), color)

        return ShortcutInfo.Builder(context, address)
                .setShortLabel(name)
                .setLongLabel(name)
                .setIcon(Icon.createWithBitmap(drawable.getBitmap()))
                .setIntents(arrayOf(
                        Intent(Intent.ACTION_MAIN, Uri.EMPTY, context, ConversationsActivity::class.java)
                                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK),
                        Intent(Intent.ACTION_VIEW, Uri.EMPTY, context, ChatActivity::class.java)
                                .putExtra(ChatActivity.EXTRA_ADDRESS, address)
                ))
                .build()
    }

    @RequiresApi(Build.VERSION_CODES.N_MR1)
    private fun removeLatestIfNeeded(newShortcutId: String) = shortcutManager?.let { manager ->

        manager.removeDynamicShortcuts(Arrays.asList(newShortcutId))

        val conversations = manager.dynamicShortcuts.asSequence()
                .filter { it.id != idSearch }
                .sortedByDescending { it.lastChangedTimestamp }
                .toList()

        if (conversations.size == 2) {
            shortcutManager?.removeDynamicShortcuts(Arrays.asList(conversations[1].id))
        }
    }
}
