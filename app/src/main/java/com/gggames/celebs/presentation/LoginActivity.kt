package com.gggames.celebs.presentation

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import com.gggames.celebs.R
import com.gggames.celebs.core.GameFlow
import kotlinx.android.synthetic.main.activity_login.*
import timber.log.Timber

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        loginUsernameEditText.setOnEditorActionListener { v, actionId, event ->
            return@setOnEditorActionListener if (actionId == EditorInfo.IME_ACTION_DONE) {
                val imm: InputMethodManager = v.context
                    .getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(v.windowToken, 0)
                onDoneClick()
                true
            } else false
        }
        buttonDone.setOnClickListener {
            onDoneClick()
        }
    }

    private fun onDoneClick() {
        if (loginUsername.editText?.text?.isNotEmpty() == true) {
            val name = loginUsername.editText?.text.toString()
            GameFlow.login(name)
            goToMainActivity(name)
        } else {
            loginUsername.error = "Please enter your name"
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
