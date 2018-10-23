package com.glodanif.bluetoothchat.data.model

import com.glodanif.bluetoothchat.data.entity.Profile

interface ProfileRepository {
    fun saveProfile(profile: Profile)
    fun getProfile(): Profile
}