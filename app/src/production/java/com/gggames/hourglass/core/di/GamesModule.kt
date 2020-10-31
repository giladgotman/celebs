package com.gggames.hourglass.core.di

import com.gggames.hourglass.features.games.data.GamesDataSource
import com.gggames.hourglass.features.games.data.GamesRepository
import com.gggames.hourglass.features.games.data.GamesRepositoryImpl
import com.gggames.hourglass.features.games.data.remote.FirebaseGamesDataSource
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
