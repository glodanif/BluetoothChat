package com.glodanif.bluetoothchat.ui.viewmodel

import android.content.Context
import androidx.core.content.ContextCompat
import com.amulyakhare.textdrawable.TextDrawable
import com.glodanif.bluetoothchat.R
import com.glodanif.bluetoothchat.data.entity.Conversation
import com.glodanif.bluetoothchat.domain.entity.Profile
import com.glodanif.bluetoothchat.ui.viewmodel.ContactViewModel
import com.glodanif.bluetoothchat.utils.getFirstLetter

class ProfileConverter(private val context: Context) {

    fun transform(profile: Profile): ProfileViewModel {

        val label = if (profile.name.isEmpty()) context.getString(R.string.profile__your_name) else profile.name
        val labelColor = ContextCompat.getColor(context, if (profile.name.isEmpty()) R.color.text_light else R.color.text_dark)
        val avatarDrawable = TextDrawable.builder().buildRound(profile.name.getFirstLetter(), profile.color)

        return ProfileViewModel(label, labelColor, avatarDrawable, profile.color)
    }
}
