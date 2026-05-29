package com.nfaralli.particleflow

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.FrameLayout
import android.widget.Spinner

class SettingsView(context: Context) : FrameLayout(context) {

    private val mNumParticles: ValidatedEditText
    private val mParticleSize: ValidatedEditText
    private val mNumAttPoints: ValidatedEditText
    private val mF01Attraction: ValidatedEditText
    private val mF01Drag: ValidatedEditText
    private val mBGColor: ColorView
    private val mSlowPColor: ColorView
    private val mFastPColor: ColorView
    private val mBGGradientView: GradientView
    private val mPartGradientView: GradientView
    private val mHueDirection: Spinner
    private val mPrefs: android.content.SharedPreferences

    init {
        addView(LayoutInflater.from(context).inflate(R.layout.settings, null))
        mNumParticles = findViewById(R.id.numParticles)
        mNumParticles.setMinValue(1)
        mNumParticles.setMaxValue(ParticlesSurfaceView.MAX_NUM_PARTICLES)
        mParticleSize = findViewById(R.id.particleSize)
        mParticleSize.setMinValue(1)
        mParticleSize.setMaxValue(50)
        mNumAttPoints = findViewById(R.id.numAPoints)
        mNumAttPoints.setMinValue(1)
        mNumAttPoints.setMaxValue(ParticlesSurfaceView.MAX_MAX_NUM_ATT_POINTS)
        mBGColor = findViewById(R.id.bgColor)
        mSlowPColor = findViewById(R.id.slowColor)
        mFastPColor = findViewById(R.id.fastColor)
        mBGGradientView = findViewById(R.id.bgGradientView)
        mPartGradientView = findViewById(R.id.gradientView)
        mHueDirection = findViewById(R.id.hueDirection)
        mF01Attraction = findViewById(R.id.f01_attraction)
        mF01Attraction.setMinValue(0)
        mF01Attraction.setMaxValue(1000)
        mF01Drag = findViewById(R.id.f01_drag)
        mF01Drag.setMinValue(0)
        mF01Drag.setMaxValue(100)
        mPrefs = context.getSharedPreferences(ParticlesSurfaceView.SHARED_PREFS_NAME, Context.MODE_PRIVATE)

        mBGColor.setOnValueChangedListener {
            mBGGradientView.setLeftColor(it)
            mBGGradientView.setRightColor(it)
            mBGGradientView.invalidate()
        }
        mSlowPColor.setOnValueChangedListener {
            mPartGradientView.setLeftColor(it)
            mPartGradientView.invalidate()
        }
        mFastPColor.setOnValueChangedListener {
            mPartGradientView.setRightColor(it)
            mPartGradientView.invalidate()
        }
        mHueDirection.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                mPartGradientView.setHueDirection(position == 0)
                mPartGradientView.invalidate()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
        findViewById<View>(R.id.resetButton).setOnClickListener {
            loadDefaultValues()
        }

        loadValues()
    }

    fun loadValues() {
        mNumParticles.setText(mPrefs.getInt("NumParticles", ParticlesSurfaceView.DEFAULT_NUM_PARTICLES).toString())
        mParticleSize.setText(mPrefs.getInt("ParticleSize", ParticlesSurfaceView.DEFAULT_PARTICLE_SIZE).toString())
        mNumAttPoints.setText(mPrefs.getInt("NumAttPoints", ParticlesSurfaceView.DEFAULT_MAX_NUM_ATT_POINTS).toString())
        mBGColor.setColor(mPrefs.getInt("BGColor", ParticlesSurfaceView.DEFAULT_BG_COLOR))
        mSlowPColor.setColor(mPrefs.getInt("SlowColor", ParticlesSurfaceView.DEFAULT_SLOW_COLOR))
        mFastPColor.setColor(mPrefs.getInt("FastColor", ParticlesSurfaceView.DEFAULT_FAST_COLOR))
        mHueDirection.setSelection(mPrefs.getInt("HueDirection", ParticlesSurfaceView.DEFAULT_HUE_DIRECTION))
        mF01Attraction.setText(mPrefs.getInt("F01Attraction", ParticlesSurfaceView.DEFAULT_F01_ATTRACTION_COEF).toString())
        mF01Drag.setText(mPrefs.getInt("F01Drag", ParticlesSurfaceView.DEFAULT_F01_DRAG_COEF).toString())
    }

    fun loadDefaultValues() {
        mNumParticles.setText(ParticlesSurfaceView.DEFAULT_NUM_PARTICLES.toString())
        mParticleSize.setText(ParticlesSurfaceView.DEFAULT_PARTICLE_SIZE.toString())
        mNumAttPoints.setText(ParticlesSurfaceView.DEFAULT_MAX_NUM_ATT_POINTS.toString())
        mBGColor.setColor(ParticlesSurfaceView.DEFAULT_BG_COLOR)
        mSlowPColor.setColor(ParticlesSurfaceView.DEFAULT_SLOW_COLOR)
        mFastPColor.setColor(ParticlesSurfaceView.DEFAULT_FAST_COLOR)
        mHueDirection.setSelection(ParticlesSurfaceView.DEFAULT_HUE_DIRECTION)
        mF01Attraction.setText(ParticlesSurfaceView.DEFAULT_F01_ATTRACTION_COEF.toString())
        mF01Drag.setText(ParticlesSurfaceView.DEFAULT_F01_DRAG_COEF.toString())
    }

    fun saveValues() {
        val focusedChild = focusedChild
        focusedChild?.clearFocus()
        val editor = mPrefs.edit()
        editor.putInt("NumParticles", Integer.parseInt(mNumParticles.text.toString()))
        editor.putInt("ParticleSize", Integer.parseInt(mParticleSize.text.toString()))
        editor.putInt("NumAttPoints", Integer.parseInt(mNumAttPoints.text.toString()))
        editor.putInt("BGColor", mBGColor.getColor())
        editor.putInt("SlowColor", mSlowPColor.getColor())
        editor.putInt("FastColor", mFastPColor.getColor())
        editor.putInt("HueDirection", mHueDirection.selectedItemPosition)
        editor.putInt("F01Attraction", Integer.parseInt(mF01Attraction.text.toString()))
        editor.putInt("F01Drag", Integer.parseInt(mF01Drag.text.toString()))
        editor.apply()
    }
}
