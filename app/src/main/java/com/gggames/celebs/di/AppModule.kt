package com.gggames.celebs.di

import android.content.Context
import com.gggames.celebs.R
import com.gggames.celebs.core.CelebsApplication
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

}