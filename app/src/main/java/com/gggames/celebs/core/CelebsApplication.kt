package com.gggames.celebs.core

import android.app.Application
import com.gggames.celebs.BuildConfig
import timber.log.Timber

class CelebsApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initTimber()
        Timber.i("onCreate")
    }


    private fun initTimber() {
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}