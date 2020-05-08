package com.gggames.celebs.core.di

import android.content.Context
import com.gggames.celebs.core.CelebsApplication
import com.gggames.celebs.presentation.di.ViewComponent
import com.gggames.celebs.presentation.login.LoginActivity
import dagger.Component
import javax.inject.Qualifier
import javax.inject.Singleton


fun getAppComponent(context: Context): AppComponent =
    (context.applicationContext as CelebsApplication).appComponent


@Singleton
@Component (modules = [AppModule::class, SubComponentsModule::class])
interface AppComponent {
    @AppContext
    fun context(): Context

    fun inject(loginActivity: LoginActivity)

    fun viewComponent(): ViewComponent.Builder
}


@Qualifier
@kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
annotation class AppContext