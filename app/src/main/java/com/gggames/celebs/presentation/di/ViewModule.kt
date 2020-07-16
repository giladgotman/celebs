package com.gggames.celebs.presentation.di

import android.content.Context
import com.gggames.celebs.presentation.endgame.GameOverPresenter
import com.gggames.celebs.presentation.endgame.GameOverScreenContract
import dagger.Module
import dagger.Provides

@Module
class ViewModule(private val context: Context) {

    @Provides
    @ViewContext
    fun provideContext(): Context = context

    @Provides
    fun provideGameOverPresenter(presenter: GameOverPresenter): GameOverScreenContract.Presenter = presenter

}