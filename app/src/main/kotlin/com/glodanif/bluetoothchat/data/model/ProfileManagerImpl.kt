package com.glodanif.bluetoothchat.data.model

import android.content.Context
import androidx.core.content.ContextCompat
import com.glodanif.bluetoothchat.R

class ProfileManagerImpl(context: Context) : ProfileManager {

    private val keyPreferences = "key.profileManager"

    private val keyUserName = "key.user_name"
    private val keyUserColor = "key.user_color"

    private val defaultAvatarBackgroundColor =
            ContextCompat.getColor(context, R.color.background_avatar_default)

    private val preferences
            by lazy { context.getSharedPreferences(keyPreferences, Context.MODE_PRIVATE) }

    override fun saveUserName(name: String) {
        preferences.edit().putString(keyUserName, name).apply()
    }

    override fun saveUserColor(color: Int) {
        preferences.edit().putInt(keyUserColor, color).apply()
    }

    override fun getUserName(): String = preferences.getString(keyUserName, "") ?: ""

    override fun getUserColor() = preferences.getInt(keyUserColor, defaultAvatarBackgroundColor)

    override fun isInitialized(): Boolean {
        val userName = preferences.getString(keyUserName, "")
        return !userName.isNullOrEmpty()
    }
}
