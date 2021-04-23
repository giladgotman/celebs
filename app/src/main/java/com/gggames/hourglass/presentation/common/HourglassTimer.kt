package com.gggames.hourglass.presentation.common

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.core.view.isVisible
import com.gggames.hourglass.R
import com.gggames.hourglass.model.TurnState
import com.gggames.hourglass.model.TurnState.*
import com.gggames.hourglass.presentation.gameon.TURN_TIME_MILLIS
import kotlinx.android.synthetic.main.hourglass_timer.view.*
import timber.log.Timber

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
        inflateView(context, attrs)
    }

    private fun inflateView(context: Context, attrs: AttributeSet?) {
        LayoutInflater.from(context).inflate(R.layout.hourglass_timer, this, true)
    }

    private fun render(state: State) {
        Timber.w("rrr state:$state")

        val seconds = (state.time / 1000).toInt() % 60
        when (seconds) {
            in 50..59 -> hourglassImage.setImageResource(R.drawable.ic_hourglass_59)
            in 40..49 -> hourglassImage.setImageResource(R.drawable.ic_hourglass_49)
            in 30..39 -> hourglassImage.setImageResource(R.drawable.ic_hourglass_39)
            in 20..29 -> hourglassImage.setImageResource(R.drawable.ic_hourglass_29)
            in 10..19 -> hourglassImage.setImageResource(R.drawable.ic_hourglass_19)
            in 4..9 -> hourglassImage.setImageResource(R.drawable.ic_hourglass_9)
            in 1..3 -> hourglassImage.setImageResource(R.drawable.ic_hourglass_3)
        }

        when (state.turnState) {
            Running -> {
                sand.isVisible = true
                when (seconds % 3) {
                    0 -> sand.setImageResource(R.drawable.ic_hourglass_sand_1)
                    1 -> sand.setImageResource(R.drawable.ic_hourglass_sand_2)
                    2 -> sand.setImageResource(R.drawable.ic_hourglass_sand_3)
                }
            }
            Over,
            Idle -> {
                sand.isVisible = false
                hourglassImage.setImageResource(R.drawable.ic_hourglass_100)
            }
            Paused -> {
                sand.isVisible = false

            }
        }
    }

    data class State(
        val time: Long = 0,
        val turnTime: Long = TURN_TIME_MILLIS,
        val turnState: TurnState = Idle
    )
}

