package com.gggames.hourglass.presentation.onboarding

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.gggames.hourglass.R
import com.gggames.hourglass.core.CelebsApplication
import com.gggames.hourglass.presentation.MainActivity
import kotlinx.android.synthetic.main.activity_welcome.*
import java.util.*
import javax.inject.Inject

class WelcomeActivity : AppCompatActivity(), WelcomeContract.View {

    @Inject
    lateinit var presenter: WelcomePresenter

    private val CAROUSEL_SWIPE_DURATION = 6L

    override fun onCreate(savedInstanceState: Bundle?) {
        (application as CelebsApplication).appComponent.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)
        initializeCarouselViewPager()
        presenter.bind(this)
    }


    private fun initializeCarouselViewPager() {
        val carouselItems = Arrays.asList(
            WelcomePagerCarouselAdapter.CarouselItem(R.layout.welcome_carousel_item1)
        )
        view_pager_carousel.adapter =
            WelcomePagerCarouselAdapter(this, carouselItems)
        view_pager_carousel.offscreenPageLimit = carouselItems.size - 1

        carousel_indicator.setViewPager(view_pager_carousel)
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