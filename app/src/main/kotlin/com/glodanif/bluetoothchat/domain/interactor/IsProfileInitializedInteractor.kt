package com.glodanif.bluetoothchat.domain.interactor

import com.glodanif.bluetoothchat.data.model.ProfileRepository

class IsProfileInitializedInteractor(private val profileRepository: ProfileRepository): BaseInteractor<Unit, Boolean>() {

    override suspend fun execute(input: Unit) = profileRepository.isInitialized()
}
