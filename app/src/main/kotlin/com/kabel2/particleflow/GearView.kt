package com.kabel2.particleflow

import android.content.Context
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView

class GearView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : AppCompatImageView(context, attrs) {

    private var mGearIndex = -1
    private val mGearIds = intArrayOf(
        R.drawable.gear_icon_00,
        R.drawable.gear_icon_10,
        R.drawable.gear_icon_20,
        R.drawable.gear_icon_30,
        R.drawable.gear_icon_40,
        R.drawable.gear_icon_50
    )

    private val mCountDownTimer = object : CountDownTimer(4000, 4000) {
        override fun onTick(millisUntilFinished: Long) {}
        override fun onFinish() {
            hideGear()
        }
    }

    private val mHandler = Handler(Looper.getMainLooper())
    private val mDrawGear = Runnable { updateGear() }

    init {
        hideGear()
    }

    val isGearVisible: Boolean
        get() = mGearIndex >= 0

    fun showGear() {
        if (mGearIndex < 0) {
            mGearIndex = 0
        }
        mCountDownTimer.start()
        updateGear()
    }

    fun hideGear() {
        mCountDownTimer.cancel()
        mHandler.removeCallbacks(mDrawGear)
        mGearIndex = -1
        setImageResource(R.drawable.gear_icon_empty)
    }

    private fun updateGear() {
        setImageResource(mGearIds[mGearIndex])
        mGearIndex = (mGearIndex + 1) % mGearIds.size
        mHandler.removeCallbacks(mDrawGear)
        mHandler.postDelayed(mDrawGear, (1000 / 30).toLong())
    }
}
