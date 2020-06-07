package com.gggames.celebs.presentation

import android.content.Context
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageButton
import com.gggames.celebs.R
import com.gggames.celebs.presentation.gameon.GameScreenContract.ButtonState

class PlayButton: AppCompatImageButton {

    var state: ButtonState = ButtonState.Stopped
    set(value) {
        field = value
        renderState()
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        if (enabled) {
            this.colorFilter = null
        } else {
            val matrix = ColorMatrix()
            matrix.setSaturation(0f)
            val filter = ColorMatrixColorFilter(matrix)
            this.colorFilter = filter
        }

    }

    constructor(context: Context) : this(context, null, 0)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )
    private fun renderState() {
        val drawable = when (state) {
            ButtonState.Stopped -> resources.getDrawable(R.drawable.start_button, null)
            ButtonState.Running -> resources.getDrawable(R.drawable.pasue_button, null)
            ButtonState.Paused -> resources.getDrawable(R.drawable.start_button, null)
            ButtonState.Finished -> resources.getDrawable(R.drawable.ic_close_24px, null)
        }
        this.setImageDrawable(drawable)
    }

}