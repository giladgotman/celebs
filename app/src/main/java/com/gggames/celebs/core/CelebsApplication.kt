package com.gggames.celebs.core

import android.app.Application
import com.gggames.celebs.BuildConfig
import com.gggames.celebs.di.AppComponent
import com.gggames.celebs.di.DaggerAppComponent
import timber.log.Timber

class CelebsApplication : Application() {

    val appComponent: AppComponent = DaggerAppComponent.create()

    override fun onCreate() {
        super.onCreate()
        initTimber()
        setupDagger()
        GameFlow.setContext(applicationContext)
        Timber.i("onCreate")
    }

    private fun setupDagger() {

    }


    private fun initTimber() {
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}