package com.glodanif.bluetoothchat.ui.fragment

import android.os.Bundle
import android.preference.PreferenceFragment
import com.glodanif.bluetoothchat.R

class SettingsFragment : PreferenceFragment() {

    override fun onCreate(paramBundle: Bundle?) {
        super.onCreate(paramBundle)
        addPreferencesFromResource(R.xml.preferences)
    }
}
