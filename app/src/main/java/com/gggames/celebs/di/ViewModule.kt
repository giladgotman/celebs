package com.gggames.celebs.di

import android.content.Context
import dagger.Module
import dagger.Provides

@Module
class ViewModule(private val context: Context) {

    @Provides
    @ViewContext
    fun provideContext(): Context = context

}