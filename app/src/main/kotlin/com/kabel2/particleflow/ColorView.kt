package com.kabel2.particleflow

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout

class ColorView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    fun interface OnValueChangedListener {
        fun onValueChanged(color: Int)
    }

    private val mRedText: ValidatedEditText
    private val mGreenText: ValidatedEditText
    private val mBlueText: ValidatedEditText
    private var mOnValueChangedListener: OnValueChangedListener? = null

    init {
        addView(LayoutInflater.from(context).inflate(R.layout.color, null))
        mRedText = findViewById(R.id.bgColorR)
        mGreenText = findViewById(R.id.bgColorG)
        mBlueText = findViewById(R.id.bgColorB)

        val onTextChangedListener = ValidatedEditText.OnTextChangedListener {
            onValueChanged()
        }
        mRedText.setOnTextChangedListener(onTextChangedListener)
        mRedText.setMinValue(0)
        mRedText.setMaxValue(255)
        mGreenText.setOnTextChangedListener(onTextChangedListener)
        mGreenText.setMinValue(0)
        mGreenText.setMaxValue(255)
        mBlueText.setOnTextChangedListener(onTextChangedListener)
        mBlueText.setMinValue(0)
        mBlueText.setMaxValue(255)
        setColor(0xFF000000.toInt())
    }

    fun setOnValueChangedListener(listener: OnValueChangedListener?) {
        mOnValueChangedListener = listener
    }

    private fun onValueChanged() {
        mOnValueChangedListener?.onValueChanged(getColor())
    }

    fun getColor(): Int {
        val r = Integer.parseInt(mRedText.text.toString())
        val g = Integer.parseInt(mGreenText.text.toString())
        val b = Integer.parseInt(mBlueText.text.toString())
        return Color.rgb(r, g, b)
    }

    fun setColor(color: Int) {
        mRedText.setText(Color.red(color).toString())
        mGreenText.setText(Color.green(color).toString())
        mBlueText.setText(Color.blue(color).toString())
        onValueChanged()
    }
}
