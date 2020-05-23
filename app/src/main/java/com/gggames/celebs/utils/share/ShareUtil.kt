package com.idagio.app.core.utils.share

import android.app.Activity
import android.content.Intent
import android.net.Uri
import com.gggames.celebs.R
import com.google.firebase.dynamiclinks.ShortDynamicLink
import com.google.firebase.dynamiclinks.ktx.androidParameters
import com.google.firebase.dynamiclinks.ktx.dynamicLinks
import com.google.firebase.dynamiclinks.ktx.shortLinkAsync
import com.google.firebase.ktx.Firebase
import io.reactivex.Single

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

@Suppress("ThrowableNotThrown")
fun getDynamicUri(uri: Uri, minAppVersion: Int = 9): Single<Uri> {
    return Single.create { emitter ->
        Firebase.dynamicLinks.shortLinkAsync(ShortDynamicLink.Suffix.SHORT) {
            link = uri
            domainUriPrefix = "https://gglab.page.link"
            // Open links with this app on Android
            androidParameters {
                minimumVersion = minAppVersion
            }
            // Open links with com.example.ios on iOS
            // iosParameters("com.example.ios") { }
        }.addOnSuccessListener { result ->
            val shortLink = result.shortLink
            shortLink?.let {
                emitter.onSuccess(shortLink)
            } ?: emitter.onError(IllegalArgumentException("short link is null"))
        }.addOnFailureListener {
            emitter.onError(it)
        }
    }
}
