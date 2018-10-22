package com.glodanif.bluetoothchat.domain.interactor

import com.glodanif.bluetoothchat.data.model.ProfileRepository
import com.glodanif.bluetoothchat.data.entity.Profile

class GetProfileInteractor(private val profileRepository: ProfileRepository): BaseInteractor<Unit, Profile>() {

    override suspend fun execute(input: Unit) = profileRepository.getProfile()
}
