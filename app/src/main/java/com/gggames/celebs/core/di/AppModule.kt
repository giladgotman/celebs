package com.gggames.celebs.core.di

import android.content.Context
import com.gggames.celebs.R
import com.gggames.celebs.core.CelebsApplication
import com.idagio.app.core.utils.rx.scheduler.BaseSchedulerProvider
import com.idagio.app.core.utils.rx.scheduler.SchedulerProvider
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class AppModule(private val application: CelebsApplication) {

    @Provides
    @Singleton
    fun provideApplication() = application

    @Provides
    @Singleton
    @AppContext
    fun provideAppContext(application: CelebsApplication) = application.applicationContext

    @Provides
    @Singleton
    fun provideSharedPreferences(@AppContext context: Context) = context.getSharedPreferences(
        context.getString(R.string.shared_prefs_default), Context.MODE_PRIVATE
    )

    @Provides
    @Singleton
    fun provideBaseSchedulerProvider(): BaseSchedulerProvider = SchedulerProvider()
}
