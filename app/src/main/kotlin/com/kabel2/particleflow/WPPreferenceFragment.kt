package com.kabel2.particleflow

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat

class WPPreferenceFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.wallpaper_preference, rootKey)
    }
}
