package com.gggames.celebs.utils

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.DrawableRes
import com.gggames.celebs.R
import javax.annotation.Nullable

@SuppressLint("InflateParams")
fun showErrorToast(context: Context, message: String, length: Int = Toast.LENGTH_SHORT): Toast =
    showToast(
        context,
        message,
        length,
        R.drawable.toast_frame_background_error
    )

@SuppressLint("ShowToast", "InflateParams")
fun showInfoToast(context: Context, message: String, length: Int = Toast.LENGTH_SHORT): Toast =
    showToast(
        context,
        message,
        length,
        R.drawable.toast_frame_background_info
    )

@SuppressLint("InflateParams")
private fun showToast(
    context: Context,
    message: String,
    length: Int,
    @DrawableRes drawableId: Int
): Toast {
    val inflater = LayoutInflater.from(context)
    val layout = inflater.inflate(R.layout.toast, null)
    @Nullable val messageView = layout.findViewById<TextView>(R.id.toast_message)
    layout.background = context.getDrawable(drawableId)
    messageView.text = message
    return Toast.makeText(context, message, length).apply {
        view = layout
        show()
    }
}
