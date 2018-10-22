package com.glodanif.bluetoothchat.domain.interactor

import com.glodanif.bluetoothchat.data.model.ProfileRepository
import com.glodanif.bluetoothchat.domain.entity.Profile

class GetProfileInteractor(private val profileRepository: ProfileRepository): BaseInteractor<NoInput, Profile>() {

    override suspend fun execute(input: NoInput) = profileRepository.getProfile()
}
