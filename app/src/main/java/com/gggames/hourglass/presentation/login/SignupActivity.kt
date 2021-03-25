package com.gggames.hourglass.presentation.login

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import com.gggames.hourglass.BuildConfig
import com.gggames.hourglass.R
import com.gggames.hourglass.core.Authenticator
import com.gggames.hourglass.core.CelebsApplication
import com.gggames.hourglass.features.user.domain.Signup
import com.gggames.hourglass.features.user.domain.SignupResponse
import com.gggames.hourglass.presentation.MainActivity
import com.gggames.hourglass.utils.showErrorToast
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_signup.*
import timber.log.Timber
import javax.inject.Inject

class SignupActivity : AppCompatActivity() {

    @Inject
    lateinit var authenticator: Authenticator

    @Inject
    lateinit var signup: Signup

    private val disposables = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        (application as CelebsApplication).appComponent.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

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

        versionValue.text = getString(R.string.version_value, BuildConfig.VERSION_NAME)

        loginUsernameEditText.requestFocus()
    }

    private fun onDoneClick() {
        if (loginUsername.editText?.text?.isNotEmpty() == true) {
            val username = loginUsername.editText?.text.toString()
            signup(username).subscribe({ response ->
                when (response) {
                    is SignupResponse.Success -> {
                        goToMainActivity(username)
                    }
                    is SignupResponse.UserAlreadyExists ->
                        loginUsername.error = "This username is already used"
                    else -> {
                        showErrorToast(this, R.string.error_generic)
                    }
                }
            }, {
                Timber.e(it, "error while trying to signup")
                showErrorToast(this, R.string.error_generic)
            }).let { disposables.add(it) }
        } else {
            loginUsername.error = "Please enter your nickname"
        }
    }

    private fun goToMainActivity(name: String) {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    companion object {

        fun createIntent(context: Context) = Intent(context, SignupActivity::class.java)

        fun start(context: Context) {
            context.startActivity(createIntent(context))
        }
    }
}
