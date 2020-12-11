package com.gggames.hourglass.core

import android.app.Application
import android.content.Context
import com.gggames.hourglass.BuildConfig
import com.gggames.hourglass.core.di.AppComponent
import com.gggames.hourglass.core.di.AppModule
import com.gggames.hourglass.core.di.DaggerAppComponent
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
        Timber.i("onCreate")
    }

    private fun initTimber() {
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }

    companion object {
        operator fun get(context: Context): CelebsApplication {
            return context.applicationContext as CelebsApplication
        }
    }
}
