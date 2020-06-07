package com.gggames.celebs.presentation

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageButton
import com.gggames.celebs.R
import com.gggames.celebs.presentation.gameon.GameScreenContract.ButtonState

class PlayButton: AppCompatImageButton {

    var state: ButtonState = ButtonState.Stopped

    constructor(context: Context) : this(context, null, 0)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )
    fun renderState(state: ButtonState) {
        when (state) {
            ButtonState.Stopped -> setImageDrawable(resources.getDrawable(R.drawable.start_button, null))
            ButtonState.Running -> setImageDrawable(resources.getDrawable(R.drawable.start_button, null))
            ButtonState.Paused -> setImageDrawable(resources.getDrawable(R.drawable.start_button, null))
        }
    }

}