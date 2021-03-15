package com.gggames.hourglass.presentation.common

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.gggames.hourglass.R
import com.gggames.hourglass.model.PlayerTurnState
import com.gggames.hourglass.presentation.di.createViewComponent
import kotlinx.android.synthetic.main.name_badge.view.*

class NameBadge : ConstraintLayout {


    var state: State  = State()
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
        LayoutInflater.from(context).inflate(R.layout.name_badge, this, true)
    }

    fun render(state: State) {
        name.text = state.name

        when (state.turnState) {
            PlayerTurnState.Idle -> name.background = context.getDrawable(R.drawable.name_badge_bg_idle)
            PlayerTurnState.Playing -> name.background = context.getDrawable(R.drawable.name_badge_bg_playing)
            PlayerTurnState.UpNext -> name.background = context.getDrawable(R.drawable.name_badge_bg_upnext)
        }
    }

    data class State(
        val name: String ="",
        val turnState: PlayerTurnState = PlayerTurnState.Idle
    )
}

