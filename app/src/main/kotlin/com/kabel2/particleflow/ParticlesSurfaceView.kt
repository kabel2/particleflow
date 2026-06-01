package com.kabel2.particleflow

import android.content.Context
import android.content.SharedPreferences
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import android.view.MotionEvent

open class ParticlesSurfaceView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : GLSurfaceView(context, attrs), SharedPreferences.OnSharedPreferenceChangeListener {

    companion object {
        const val SHARED_PREFS_NAME = "particleFlowPrefs"
        const val DEFAULT_NUM_PARTICLES = 50000
        const val MAX_NUM_PARTICLES = 1000000
        const val DEFAULT_PARTICLE_SIZE = 1
        const val DEFAULT_MAX_NUM_ATT_POINTS = 5
        const val MAX_MAX_NUM_ATT_POINTS = 16
        const val DEFAULT_BG_COLOR = 0xFF000000.toInt()
        const val DEFAULT_SLOW_COLOR = 0xFF4C4CFF.toInt()
        const val DEFAULT_FAST_COLOR = 0xFFFF4C4C.toInt()
        const val DEFAULT_HUE_DIRECTION = 0
        const val DEFAULT_F01_ATTRACTION_COEF = 100
        const val DEFAULT_F01_DRAG_COEF = 4
    }

    private val mRenderer: ParticlesRenderer
    private var mCount: IntArray
    private val mPrefs: SharedPreferences

    init {
        setEGLContextClientVersion(2)
        mRenderer = ParticlesRenderer(context)
        setRenderer(mRenderer)

        mPrefs = context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE)
        mPrefs.registerOnSharedPreferenceChangeListener(this)
        mCount = IntArray(mPrefs.getInt("NumAttPoints", DEFAULT_MAX_NUM_ATT_POINTS))
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onTouchEvent(e: MotionEvent): Boolean {
        when (e.action) {
            MotionEvent.ACTION_MOVE, MotionEvent.ACTION_DOWN -> {
                var ids = 0
                val numPointers = e.pointerCount
                for (i in 0 until numPointers) {
                    val pid = e.getPointerId(i)
                    ids = ids or (1 shl pid)
                    if (pid < mCount.size) {
                        mCount[pid] = 0
                        mRenderer.setTouch(pid, e.getX(i), e.getY(i))
                    }
                }
                for (i in mCount.indices) {
                    if ((ids and 1) == 0) {
                        if (mCount[i]++ >= 3) {
                            mRenderer.setTouch(i, -1.0f, -1.0f)
                        }
                    }
                    ids = ids shr 1
                }
                mRenderer.syncTouch()
                requestRender()
            }
        }
        return true
    }

    override fun onSharedPreferenceChanged(prefs: SharedPreferences, key: String?) {
        if (key == "ShowSettingsHint") {
            return
        }
        mCount = IntArray(mPrefs.getInt("NumAttPoints", DEFAULT_MAX_NUM_ATT_POINTS))
        mRenderer.onPrefsChanged()
    }

    fun resetAttractionPoints() {
        mRenderer.resetAttractionPoints()
    }
}
