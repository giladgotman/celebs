package com.gggames.hourglass.presentation.onboarding

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.gggames.hourglass.R
import com.gggames.hourglass.core.CelebsApplication
import com.gggames.hourglass.presentation.MainActivity
import com.gggames.hourglass.presentation.login.SignupActivity
import com.gggames.hourglass.utils.transitions.TRANSITION_FADE
import com.gggames.hourglass.utils.transitions.setupWindowSlideAnimations
import com.gggames.hourglass.utils.transitions.startActivityAnimated
import kotlinx.android.synthetic.main.activity_welcome.*
import javax.inject.Inject

class WelcomeActivity : AppCompatActivity(), WelcomeContract.View {

    @Inject
    lateinit var presenter: WelcomePresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        (application as CelebsApplication).appComponent.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        setupWindowSlideAnimations()

        get_started_button.setOnClickListener {
            startActivityAnimated(SignupActivity.createIntent(this), TRANSITION_FADE)
        }
        initializeCarouselViewPager()
        presenter.bind(this)
    }


    private fun initializeCarouselViewPager() {
        val carouselItems = listOf(
            WelcomePagerCarouselAdapter.CarouselItem(R.layout.welcome_carousel_item1),
            WelcomePagerCarouselAdapter.CarouselItem(R.layout.welcome_carousel_item2),
            WelcomePagerCarouselAdapter.CarouselItem(R.layout.welcome_carousel_item3)
        )
        view_pager_carousel.adapter =
            WelcomePagerCarouselAdapter(this, carouselItems)
        view_pager_carousel.offscreenPageLimit = carouselItems.size - 1

        carousel_indicator.setViewPager(view_pager_carousel)

        view_pager_carousel.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            }

            override fun onPageSelected(position: Int) {
                if (position == carouselItems.size - 1) {
                    get_started_button.visibility = View.VISIBLE
                } else {
                    get_started_button.visibility = View.INVISIBLE
                }
            }

            override fun onPageScrollStateChanged(state: Int) {
            }
        })
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