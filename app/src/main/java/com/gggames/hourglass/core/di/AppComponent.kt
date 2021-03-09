package com.gggames.hourglass.core.di

import android.content.Context
import com.gggames.hourglass.core.CelebsApplication
import com.gggames.hourglass.features.video.VideoModule
import com.gggames.hourglass.presentation.MainActivity
import com.gggames.hourglass.presentation.di.ViewComponent
import com.gggames.hourglass.presentation.login.SignupActivity
import com.gggames.hourglass.presentation.onboarding.WelcomeActivity
import dagger.Component
import javax.inject.Qualifier
import javax.inject.Singleton

fun getAppComponent(context: Context): AppComponent =
    (context.applicationContext as CelebsApplication).appComponent

@Singleton
@Component(modules = [
    AppModule::class,
    SubComponentsModule::class,
    NetworkModule::class,
    GamesModule::class,
    CardsModule::class,
    PlayersModule::class,
    UserModule::class,
    VideoModule::class
])
interface AppComponent {
    @AppContext
    fun context(): Context

    fun inject(activity: MainActivity)

    fun inject(activity: SignupActivity)

    fun inject(activity: WelcomeActivity)

    fun viewComponent(): ViewComponent.Builder
}

@Qualifier
@kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
annotation class AppContext
