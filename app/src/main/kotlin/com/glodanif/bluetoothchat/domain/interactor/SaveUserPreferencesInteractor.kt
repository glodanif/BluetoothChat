package com.glodanif.bluetoothchat.domain.interactor

import com.glodanif.bluetoothchat.data.entity.UserPreferences
import com.glodanif.bluetoothchat.data.model.UserPreferencesStorage

class SaveUserPreferencesInteractor(private val storage: UserPreferencesStorage) : BaseInteractor<UserPreferences, Unit>() {

    override suspend fun execute(input: UserPreferences) {
        storage.savePreferences(input)
    }
}
