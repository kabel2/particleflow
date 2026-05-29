package com.nfaralli.particleflow

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

class GradientView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private var mLeftColor = 0
    private var mRightColor = 0
    private var mHueClockwise = true
    private var mBitmap: Bitmap? = null
    private val mIdentityMatrix = Matrix()

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        createBitmap(w, h)
    }

    override fun onDraw(canvas: Canvas) {
        mBitmap?.let { canvas.drawBitmap(it, mIdentityMatrix, null) }
    }

    fun setLeftColor(color: Int) {
        mLeftColor = color
        createBitmap(width, height)
    }

    fun setRightColor(color: Int) {
        mRightColor = color
        createBitmap(width, height)
    }

    fun setHueDirection(clockwise: Boolean) {
        mHueClockwise = clockwise
        createBitmap(width, height)
    }

    private fun createBitmap(w: Int, h: Int) {
        if (w <= 0 || h <= 0) return
        mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(mBitmap!!)
        val paint = Paint()
        val leftHSV = FloatArray(3)
        val rightHSV = FloatArray(3)
        val hsv = FloatArray(3)

        Color.colorToHSV(mLeftColor, leftHSV)
        Color.colorToHSV(mRightColor, rightHSV)
        for (x in 0 until w) {
            val alpha = x.toFloat() / (w - 1)
            hsv[0] = getHue(alpha, leftHSV[0], rightHSV[0], mHueClockwise)
            hsv[1] = (1 - alpha) * leftHSV[1] + alpha * rightHSV[1]
            hsv[2] = (1 - alpha) * leftHSV[2] + alpha * rightHSV[2]
            paint.color = Color.HSVToColor(hsv)
            canvas.drawLine(x.toFloat(), 0f, x.toFloat(), h.toFloat(), paint)
        }
    }

    private fun getHue(alpha: Float, left: Float, right: Float, clockwise: Boolean): Float {
        var l = left
        var r = right
        if (l < r && clockwise) {
            l += 360f
        } else if (l > r && !clockwise) {
            r += 360f
        }
        var hue = (1 - alpha) * l + alpha * r
        if (hue >= 360f) {
            hue -= 360f
        }
        return hue
    }
}
