package com.gggames.hourglass.utils

import com.gggames.hourglass.BuildConfig

fun isInDebug(): Boolean =
    BuildConfig.DEBUG

fun doInDebug(block: () -> Unit): Unit =
    if (isInDebug()) {
        block()
    } else Unit
