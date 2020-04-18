package com.gggames.celebs.presentation

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.gggames.celebs.R
import com.gggames.celebs.core.GameFlow
import kotlinx.android.synthetic.main.activity_login.*
import timber.log.Timber

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        buttonDone.setOnClickListener {
            if (login_username.editText?.text?.isNotEmpty() == true) {
                val name = login_username.editText?.text.toString()
                GameFlow.login(name)
                goToMainActivity(name)
            } else {
                login_username.error = "Please enter your name"
            }
        }
    }

    private fun goToMainActivity(name: String) {
        val intent = Intent(this, MainActivity::class.java)
        val args = Bundle()
        args.putString(PLAYER_NAME_KEY, name)
        intent.putExtras(args)
        startActivity(intent)
        finish()
    }

    override fun onResume() {
        super.onResume()
        val me = GameFlow.me
        Timber.w("onResume me: $me")
        if (me != null) {
            goToMainActivity(me.name)
        }
    }
}
