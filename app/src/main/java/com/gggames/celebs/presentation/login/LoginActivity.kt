package com.gggames.celebs.presentation.login

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import com.gggames.celebs.R
import com.gggames.celebs.core.CelebsApplication
import com.gggames.celebs.core.GameFlow
import com.gggames.celebs.presentation.MainActivity
import com.gggames.celebs.presentation.creategame.PLAYER_NAME_KEY
import kotlinx.android.synthetic.main.activity_login.*
import timber.log.Timber
import javax.inject.Inject

class LoginActivity : AppCompatActivity() {

    @Inject
    lateinit var gameFlow: GameFlow

    override fun onCreate(savedInstanceState: Bundle?) {
        (application as CelebsApplication).appComponent.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        loginUsernameEditText.setOnEditorActionListener { v, actionId, _ ->
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
            gameFlow.login(name)
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
        val me = gameFlow.me
        Timber.w("onResume me: $me")
        if (me != null) {
            goToMainActivity(me.name)
        }
    }
}
