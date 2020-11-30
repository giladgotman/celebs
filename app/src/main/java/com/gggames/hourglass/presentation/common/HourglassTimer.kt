package com.gggames.hourglass.presentation.common

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.gggames.hourglass.R
import com.gggames.hourglass.model.PlayerTurnState
import com.gggames.hourglass.presentation.di.createViewComponent
import com.gggames.hourglass.presentation.gameon.TURN_TIME_MILLIS

class HourglassTimer : FrameLayout {

    var state: State = State()
        set(value) {
            field = value
            render(value)
        }

    constructor(context: Context) : this(context, null, 0)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        createViewComponent(context).inject(this)
        inflateView(context, attrs)
    }

    private fun inflateView(context: Context, attrs: AttributeSet?) {
        LayoutInflater.from(context).inflate(R.layout.hourglass_timer, this, true)
    }

    private fun render(value: State) {

    }
}

data class State(
    val time: Long = 0,
    val turnTime: Long = TURN_TIME_MILLIS,
    val turnState: PlayerTurnState = PlayerTurnState.Idle
)