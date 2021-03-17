package com.gggames.hourglass.presentation.feedback

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.gggames.hourglass.R
import kotlinx.android.synthetic.main.activity_feedback.*

class FeedbackActivity : AppCompatActivity() {

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feedback)

        feedbackWebview.settings.javaScriptEnabled = true;
        feedbackWebview.loadUrl(
            "https://i6j1aat88q8.typeform.com/to/OxJapaQX")
    }

    companion object {
        fun start(context: Context) {
            context.startActivity(Intent(context, FeedbackActivity::class.java))
        }
    }
}