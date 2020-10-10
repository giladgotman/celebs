package com.gggames.celebs.presentation.common

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.gggames.celebs.R
import com.gggames.celebs.model.PlayerTurnState
import com.gggames.celebs.presentation.di.createViewComponent
import kotlinx.android.synthetic.main.name_badge.view.*
import timber.log.Timber

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

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
    }

    fun render(state: State) {
        Timber.w("render: $state")
        name.text = state.name
    }
}

data class State(
    val name: String ="",
    val turnState: PlayerTurnState = PlayerTurnState.Idle
)