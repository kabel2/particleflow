package com.nfaralli.particleflow;

import android.os.Bundle;
import androidx.preference.PreferenceFragmentCompat;

public class WPPreferenceFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.wallpaper_preference, rootKey);
    }
}
