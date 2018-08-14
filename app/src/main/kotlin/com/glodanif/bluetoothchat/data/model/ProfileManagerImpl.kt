package com.glodanif.bluetoothchat.data.model

import android.content.Context
import android.support.v4.content.ContextCompat
import com.glodanif.bluetoothchat.R

class ProfileManagerImpl(context: Context) : ProfileManager {

    private val KEY_PREFERENCES = "key.profileManager"

    private val KEY_USER_NAME = "key.user_name"
    private val KEY_USER_COLOR = "key.user_color"

    private val defaultAvatarBackgroundColor =
            ContextCompat.getColor(context, R.color.background_avatar_default)

    private val preferences
            by lazy { context.getSharedPreferences(KEY_PREFERENCES, Context.MODE_PRIVATE) }

    override fun saveUserName(name: String) {
        preferences.edit().putString(KEY_USER_NAME, name).apply()
    }

    override fun saveUserColor(color: Int) {
        preferences.edit().putInt(KEY_USER_COLOR, color).apply()
    }

    override fun getUserName(): String = preferences.getString(KEY_USER_NAME, "") ?: ""

    override fun getUserColor() = preferences.getInt(KEY_USER_COLOR, defaultAvatarBackgroundColor)
}
