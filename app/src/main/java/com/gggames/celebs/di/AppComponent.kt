package com.gggames.celebs.di

import com.gggames.celebs.presentation.LoginActivity
import dagger.Component
import javax.inject.Qualifier

@Component
interface AppComponent {
//    @AppContext
//    fun context(): Context

    fun inject(loginActivity: LoginActivity)
}


@Qualifier
@kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
annotation class AppContext