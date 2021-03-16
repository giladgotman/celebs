package com.gggames.hourglass.utils

import android.content.Context
import android.graphics.Typeface
import android.view.View
import androidx.annotation.ColorRes
import androidx.lifecycle.LifecycleOwner
import com.gggames.hourglass.R
import com.skydoves.balloon.*

fun createToolTip(
    context: Context,
    arrowOrientation: ArrowOrientation,
    text: String,
    textSize: Float = 14f,
    lifecycleOwner: LifecycleOwner,
    animation: BalloonAnimation,
    @ColorRes backgroundColor: Int = R.color.colorAccent,
    @ColorRes textColor: Int = R.color.white,
    height: Int = 65,
    alpha: Float = 1.0f,
    clickListener: OnBalloonClickListener = object: OnBalloonClickListener {
        override fun onBalloonClick(view: View) {
        }
    }

): Balloon {
    return createBalloon(context) {
        setArrowSize(10)
        setPadding(8)
        setArrowOrientation(arrowOrientation)
        setArrowPositionRules(ArrowPositionRules.ALIGN_ANCHOR)
        setWidth(BalloonSizeSpec.WRAP)
        setHeight(height)
        setArrowPosition(0.5f)
        setCornerRadius(4f)
        setAlpha(alpha)
        setText(text)
        setTextSize(textSize)
        setTextTypeface(Typeface.BOLD)
        setTextColorResource(textColor)
        setBackgroundColorResource(backgroundColor)
        setBalloonAnimation(animation)
        setLifecycleOwner(lifecycleOwner)
        setOnBalloonClickListener (clickListener)
    }
}