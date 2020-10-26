package com.idagio.app.core.utils.share

import android.content.Context
import android.net.Uri
import com.gggames.hourglass.R
import com.gggames.hourglass.core.di.AppContext
import javax.inject.Inject

data class Shareable(
    val contentId: String,
    val title: String,
    val message: String,
    val url: String,
    val imageUrl: String?
) {
    class Factory @Inject constructor(
        @AppContext val context: Context
    ) {

        fun create(contentId: String, contentTitle: String, uri: Uri) =
            Shareable(
                contentId,
                contentTitle,
                context.getString(R.string.share_message, contentTitle, uri.toString()),
                uri.toString(),
                null
            )
    }
}
