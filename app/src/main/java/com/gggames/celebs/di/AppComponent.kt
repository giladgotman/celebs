package com.gggames.celebs.di

import android.content.Context
import com.gggames.celebs.core.CelebsApplication
import com.gggames.celebs.presentation.LoginActivity
import dagger.Component
import javax.inject.Qualifier
import javax.inject.Singleton


fun getAppComponent(context: Context): AppComponent =
    (context.applicationContext as CelebsApplication).appComponent


@Singleton
@Component (modules = [AppModule::class])
interface AppComponent {
    @AppContext
    fun context(): Context

    fun inject(loginActivity: LoginActivity)
}


@Qualifier
@kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
annotation class AppContext