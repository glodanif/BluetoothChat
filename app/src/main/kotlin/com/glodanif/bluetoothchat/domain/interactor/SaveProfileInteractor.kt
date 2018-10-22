package com.glodanif.bluetoothchat.domain.interactor

import com.glodanif.bluetoothchat.data.model.ProfileRepository
import com.glodanif.bluetoothchat.domain.entity.Profile

class SaveProfileInteractor(private val profileRepository: ProfileRepository): BaseInteractor<Profile, Unit>() {

    override suspend fun execute(input: Profile) {
        profileRepository.saveProfile(input)
    }
}
