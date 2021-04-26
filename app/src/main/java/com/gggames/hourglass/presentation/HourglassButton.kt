package com.gggames.hourglass.presentation

import android.content.Context
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageButton

open class HourglassButton : AppCompatImageButton {

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)

        if (enabled) {
            this.colorFilter = null
        } else {
            appyGreyScale()
        }
    }

    constructor(context: Context) : this(context, null, 0)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    private fun appyGreyScale() {
        val matrix = ColorMatrix()
        matrix.setSaturation(0.3f)
        val filter = ColorMatrixColorFilter(matrix)
        this.colorFilter = filter
    }

    private fun setAlpha(enabled: Boolean) {
        if (enabled) {
            this.alpha = 1f
        } else {
            this.alpha = 0.7f
        }

    }
}
