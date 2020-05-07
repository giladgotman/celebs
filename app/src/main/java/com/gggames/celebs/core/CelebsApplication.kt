package com.gggames.celebs.core

import android.app.Application
import com.gggames.celebs.BuildConfig
import com.gggames.celebs.di.AppComponent
import com.gggames.celebs.di.AppModule
import com.gggames.celebs.di.DaggerAppComponent
import timber.log.Timber

class CelebsApplication : Application() {

    private var _appComponent: AppComponent? = null

    var appComponent: AppComponent
        get() {
            if (_appComponent == null) {
                _appComponent = DaggerAppComponent.builder()
                    .appModule(AppModule(this))
                    .build()
            }
            return _appComponent!!
        }
        set(appComponent) {
            _appComponent = appComponent
        }

    override fun onCreate() {
        super.onCreate()
        initTimber()
        GameFlow.setContext(this)
        Timber.i("onCreate")
    }



    private fun initTimber() {
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}