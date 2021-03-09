package com.gggames.hourglass.presentation.onboarding

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager

class WelcomePagerAdapter(
    private val context: Context,
    private val pages: List<Page>,
    private val onViewPagerInteractionsListener: OnViewPagerInteractionsListener
) : PagerAdapter() {

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val inflater = LayoutInflater.from(context)
        val layout = inflater.inflate(pages[position].layoutResource, container, false)
        container.addView(layout)
        onViewPagerInteractionsListener.onViewPagerReady(layout)
        return layout
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        (container as ViewPager).removeView(`object` as View)
    }

    override fun getCount(): Int {
        return pages.size
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view == `object`
    }

    class Page(@LayoutRes val layoutResource: Int)

    interface OnViewPagerInteractionsListener {
        fun onViewPagerReady(layout: View)
    }
}
