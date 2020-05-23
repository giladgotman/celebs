package com.idagio.app.core.utils.share

import android.net.Uri
import javax.inject.Inject

data class Shareable(
    val contentId: String,
    val title: String,
    val message: String,
    val url: String,
    val imageUrl: String?
) {
    class Factory @Inject constructor() {
        fun create(contentId: String, contentTitle: String, uri: Uri) =
            Shareable(
                contentId,
                contentTitle,
                uri.toString(),
                uri.toString(),
                null
            )
    }
}
