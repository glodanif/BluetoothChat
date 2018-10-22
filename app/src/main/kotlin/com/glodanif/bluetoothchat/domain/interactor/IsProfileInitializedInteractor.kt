package com.glodanif.bluetoothchat.domain.interactor

import com.glodanif.bluetoothchat.data.model.ProfileRepository
import com.glodanif.bluetoothchat.domain.entity.Profile

class IsProfileInitializedInteractor(private val profileRepository: ProfileRepository): BaseInteractor<NoInput, Boolean>() {

    override suspend fun execute(input: NoInput) = profileRepository.isInitialized()
}
