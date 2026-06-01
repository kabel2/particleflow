package com.kabel2.particleflow

import android.app.Dialog
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

class MainActivity : AppCompatActivity() {

    private lateinit var mGLView: ParticlesSurfaceView
    private lateinit var mGearView: GearView
    private lateinit var mSettingsView: SettingsView
    private var mSettingsDialog: Dialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.particles)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        hideSystemBars()

        mGLView = findViewById(R.id.particles_view)
        mGearView = findViewById(R.id.gear_view)
        mSettingsView = SettingsView(this)

        ViewCompat.setOnApplyWindowInsetsListener(mGearView) { v, insets ->
            val topInset = insets.getInsets(WindowInsetsCompat.Type.systemBars()).top
            val params = v.layoutParams as android.view.ViewGroup.MarginLayoutParams
            params.topMargin = topInset
            v.layoutParams = params
            insets
        }

        mGearView.setOnClickListener {
            if (mGearView.isGearVisible) {
                mGLView.onPause()
                mGearView.hideGear()
                mSettingsDialog?.show()
            } else {
                mGearView.showGear()
            }
        }

        mSettingsDialog = AlertDialog.Builder(this)
            .setTitle(R.string.settings_title)
            .setIcon(R.drawable.gear_icon_00)
            .setView(mSettingsView)
            .setPositiveButton(R.string.ok) { _, _ ->
                mSettingsView.saveValues()
                mGLView.onResume()
            }
            .setNegativeButton(R.string.cancel) { _, _ ->
                mSettingsView.loadValues()
                mGLView.onResume()
            }
            .setOnCancelListener {
                mSettingsView.loadValues()
                mGLView.onResume()
            }
            .create()
    }

    override fun onPause() {
        super.onPause()
        mGLView.onPause()
        mGearView.hideGear()
    }

    override fun onResume() {
        super.onResume()
        hideSystemBars()
        val prefs = getSharedPreferences(ParticlesSurfaceView.SHARED_PREFS_NAME, MODE_PRIVATE)
        if (prefs.getBoolean("ShowSettingsHint", true)) {
            Toast.makeText(this, R.string.settings_hint, Toast.LENGTH_LONG).show()
            prefs.edit().putBoolean("ShowSettingsHint", false).apply()
        }
        mGLView.onResume()
    }

    private fun hideSystemBars() {
        val rootView = findViewById<View>(android.R.id.content)
        val insetsController = WindowInsetsControllerCompat(window, rootView)
        insetsController.hide(WindowInsetsCompat.Type.systemBars())
        insetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }
}
