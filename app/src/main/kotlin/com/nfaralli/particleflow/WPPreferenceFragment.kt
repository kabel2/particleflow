package com.nfaralli.particleflow

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat

class WPPreferenceFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.wallpaper_preference, rootKey)
    }
}
