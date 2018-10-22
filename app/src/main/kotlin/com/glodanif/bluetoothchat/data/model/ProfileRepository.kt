package com.glodanif.bluetoothchat.data.model

import com.glodanif.bluetoothchat.domain.entity.Profile

interface ProfileRepository {
    fun saveProfile(profile: Profile)
    fun getProfile(): Profile
    fun isInitialized(): Boolean
}