package com.kabel2.particleflow

import android.content.Context
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.appcompat.widget.AppCompatEditText

class ValidatedEditText @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : AppCompatEditText(context, attrs), TextView.OnEditorActionListener, View.OnFocusChangeListener {

    fun interface OnTextChangedListener {
        fun onTextChanged(str: String)
    }

    private var mMinValue = 0
    private var mMaxValue = 0
    private var mInitialValue = 0
    private var mOnTextChangedListener: OnTextChangedListener? = null

    init {
        setOnEditorActionListener(this)
        setOnFocusChangeListener(this)
    }

    fun setMinValue(minValue: Int) {
        mMinValue = minValue
    }

    fun setMaxValue(maxValue: Int) {
        mMaxValue = maxValue
    }

    fun setOnTextChangedListener(listener: OnTextChangedListener?) {
        mOnTextChangedListener = listener
    }

    override fun onEditorAction(v: TextView, actionId: Int, event: KeyEvent?): Boolean {
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            validateText(v as AppCompatEditText)
            return true
        }
        return false
    }

    override fun onFocusChange(v: View, hasFocus: Boolean) {
        if (hasFocus) {
            mInitialValue = Integer.parseInt((v as AppCompatEditText).text.toString())
        } else {
            validateText(v as AppCompatEditText)
        }
    }

    private fun validateText(v: AppCompatEditText) {
        val initialValueStr = v.text.toString()
        val value: Int = when {
            initialValueStr.isEmpty() -> mInitialValue
            else -> try {
                Integer.valueOf(initialValueStr)
            } catch (e: NumberFormatException) {
                mMinValue
            }
        }
        val clamped = when {
            value < mMinValue -> mMinValue
            value > mMaxValue -> mMaxValue
            else -> value
        }
        if (initialValueStr != clamped.toString()) {
            v.setText(clamped.toString())
        }
        mOnTextChangedListener?.onTextChanged(clamped.toString())
    }
}
