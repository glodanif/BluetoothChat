package com.glodanif.bluetoothchat.domain.interactor

import com.glodanif.bluetoothchat.data.model.ProfileRepository
import com.glodanif.bluetoothchat.data.service.message.Contract
import com.glodanif.bluetoothchat.domain.InvalidProfileNameException
import com.glodanif.bluetoothchat.domain.entity.Profile

class SaveProfileInteractor(private val profileRepository: ProfileRepository) : BaseInteractor<Profile, Unit>() {

    override suspend fun execute(input: Profile) {

        val name = input.name
        if (!name.isEmpty() && name.length <= 25 && !name.contains(Contract.DIVIDER)) {
            profileRepository.saveProfile(input)
        } else {
            throw InvalidProfileNameException(Contract.DIVIDER)
        }
    }
}
