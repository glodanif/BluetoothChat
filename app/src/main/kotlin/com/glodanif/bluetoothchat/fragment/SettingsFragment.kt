package com.glodanif.bluetoothchat.fragment

import android.os.Bundle
import android.preference.PreferenceFragment
import com.glodanif.bluetoothchat.R

class SettingsFragment : PreferenceFragment() {

    override fun onCreate(paramBundle: Bundle?) {
        super.onCreate(paramBundle)
        addPreferencesFromResource(R.xml.preferences)
    }
}
