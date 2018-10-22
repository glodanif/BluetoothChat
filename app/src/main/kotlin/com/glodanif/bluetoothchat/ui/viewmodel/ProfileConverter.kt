package com.glodanif.bluetoothchat.ui.viewmodel

import android.content.Context
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import com.amulyakhare.textdrawable.TextDrawable
import com.glodanif.bluetoothchat.R
import com.glodanif.bluetoothchat.data.entity.Conversation
import com.glodanif.bluetoothchat.domain.entity.Profile
import com.glodanif.bluetoothchat.ui.viewmodel.ContactViewModel
import com.glodanif.bluetoothchat.utils.getFirstLetter

class ProfileConverter(private val context: Context) {

    fun transform(profile: Profile) = transform(profile.name, profile.color)

    fun transform(name: String, @ColorInt color: Int): ProfileViewModel {

        val label = if (name.isEmpty()) context.getString(R.string.profile__your_name) else name
        val labelColor = ContextCompat.getColor(context, if (name.isEmpty()) R.color.text_light else R.color.text_dark)
        val avatarDrawable = TextDrawable.builder().buildRound(name.getFirstLetter(), color)

        return ProfileViewModel(label, labelColor, avatarDrawable, color)
    }
}
