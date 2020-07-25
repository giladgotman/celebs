package com.gggames.celebs.presentation

import android.content.Context
import android.util.AttributeSet
import com.gggames.celebs.R
import com.gggames.celebs.presentation.gameon.GameScreenContract.ButtonState

class PlayButton : HourglassButton {

    var state: ButtonState = ButtonState.Stopped
    set(value) {
        field = value
        renderState()
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
            ButtonState.Finished -> resources.getDrawable(R.drawable.exit_button, null)
        }
        this.setImageDrawable(drawable)
    }
}
