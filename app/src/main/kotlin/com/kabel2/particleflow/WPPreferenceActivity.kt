package com.kabel2.particleflow

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class WPPreferenceActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportFragmentManager
            .beginTransaction()
            .replace(android.R.id.content, WPPreferenceFragment())
            .commit()
    }
}
