package com.idagio.app.core.utils.share

import android.app.Activity
import android.content.Intent
import com.gggames.celebs.R

fun Activity.share(shareable: Shareable) {
    val activity = this
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TITLE, shareable.title)
        putExtra(Intent.EXTRA_TEXT, shareable.message)
    }
    activity.startActivity(
        Intent.createChooser(
            intent,
            activity.getString(R.string.share_with_title)
        )
    )
}
