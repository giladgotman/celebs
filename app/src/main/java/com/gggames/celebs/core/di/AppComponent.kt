package com.gggames.celebs.core.di

import android.content.Context
import com.gggames.celebs.core.CelebsApplication
import com.gggames.celebs.presentation.MainActivity
import com.gggames.celebs.presentation.di.ViewComponent
import com.gggames.celebs.presentation.login.SignupActivity
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
    UserModule::class
])
interface AppComponent {
    @AppContext
    fun context(): Context

    fun inject(activity: MainActivity)

    fun inject(activity: SignupActivity)

    fun viewComponent(): ViewComponent.Builder
}


@Qualifier
@kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
annotation class AppContext