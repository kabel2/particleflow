package com.nfaralli.particleflow;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class WPPreferenceActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(android.R.id.content, new WPPreferenceFragment())
                .commit();
    }
}
