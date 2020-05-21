package com.gggames.celebs.core.di

import com.gggames.celebs.features.games.data.GamesDataSource
import com.gggames.celebs.features.games.data.GamesRepository
import com.gggames.celebs.features.games.data.GamesRepositoryImpl
import com.gggames.celebs.features.games.data.remote.FirebaseGamesDataSource
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class GamesModule {

    @Provides
    @Singleton
    fun provideGamesRepository(
        repository: GamesRepositoryImpl
    ): GamesRepository = repository

    @Provides
    @Singleton
    fun provideGamesDataSource(
        dataSource: FirebaseGamesDataSource
    ): GamesDataSource = dataSource
}