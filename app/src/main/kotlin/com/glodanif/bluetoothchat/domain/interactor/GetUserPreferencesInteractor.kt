package com.glodanif.bluetoothchat.domain.interactor

import com.glodanif.bluetoothchat.data.entity.UserPreferences
import com.glodanif.bluetoothchat.data.model.UserPreferencesStorage

class GetUserPreferencesInteractor(private val storage: UserPreferencesStorage): BaseInteractor<Unit, UserPreferences>() {

    override suspend fun execute(input: Unit) = storage.getPreferences()
}
