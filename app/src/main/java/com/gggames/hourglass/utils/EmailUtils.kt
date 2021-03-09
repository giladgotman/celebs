package com.gggames.hourglass.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import timber.log.Timber


fun sendEmail(context: Context, to: String? = null, subject: String? = "subject", body: String? = null) {
    val builder = StringBuilder("mailto:" + Uri.encode(to))
    subject?.let {
        builder.append("?subject=" + Uri.encode(subject))
    }
    body?.let {
        builder.append("&body=" + Uri.encode(body))
    }
    val uri = builder.toString()
    val intent = Intent(Intent.ACTION_SENDTO, Uri.parse(uri))
    try {
        context.startActivity(intent)
    } catch (e: Exception) {
        Timber.e(e, "Exception while trying to send feedback")
    }
}