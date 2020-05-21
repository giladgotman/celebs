package com.gggames.celebs.presentation.di

import android.content.Context
import com.gggames.celebs.presentation.di.ViewContext
import dagger.Module
import dagger.Provides

@Module
class ViewModule(private val context: Context) {

    @Provides
    @ViewContext
    fun provideContext(): Context = context

}