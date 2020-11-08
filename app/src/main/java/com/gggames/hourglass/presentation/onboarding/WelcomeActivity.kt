package com.gggames.hourglass.presentation.onboarding

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.gggames.hourglass.R
import com.gggames.hourglass.core.CelebsApplication
import com.gggames.hourglass.presentation.MainActivity
import javax.inject.Inject

class WelcomeActivity : AppCompatActivity(), WelcomeContract.View {

    @Inject
    lateinit var presenter: WelcomePresenter

    private val CAROUSEL_SWIPE_DURATION = 6L

    private var mainViewPager: View? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        (application as CelebsApplication).appComponent.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)
        presenter.bind(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.unBind()
    }

    override fun redirectToMain() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}