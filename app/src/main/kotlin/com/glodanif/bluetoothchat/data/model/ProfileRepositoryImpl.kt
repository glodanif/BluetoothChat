package com.glodanif.bluetoothchat.data.model

import android.content.Context
import androidx.core.content.ContextCompat
import com.glodanif.bluetoothchat.R
import com.glodanif.bluetoothchat.data.entity.Profile

class ProfileRepositoryImpl(context: Context) : ProfileRepository {

    private val keyPreferences = "key.profileManager"

    private val keyUserName = "key.user_name"
    private val keyUserColor = "key.user_color"

    private val defaultAvatarBackgroundColor =
            ContextCompat.getColor(context, R.color.background_avatar_default)

    private val preferences
            by lazy { context.getSharedPreferences(keyPreferences, Context.MODE_PRIVATE) }


    override fun getProfile() = Profile(
            preferences.getString(keyUserName, "") ?: "",
            preferences.getInt(keyUserColor, defaultAvatarBackgroundColor)
    )

    override fun saveProfile(profile: Profile) {
        preferences.edit()
                .putString(keyUserName, profile.name)
                .putInt(keyUserColor, profile.color)
                .apply()
    }

    override fun isInitialized(): Boolean {
        val userName = preferences.getString(keyUserName, "")
        return !userName.isNullOrEmpty()
    }
}
