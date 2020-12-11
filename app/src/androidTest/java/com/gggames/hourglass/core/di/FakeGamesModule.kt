package com.gggames.hourglass.core.di

import com.gggames.hourglass.features.games.data.GamesRepository
import com.gggames.hourglass.features.games.data.GamesRepositoryImpl
import com.gggames.hourglass.features.games.data.memory.GamesMemoryDataSource
import com.gggames.hourglass.features.games.data.memory.InMemoryGamesDataSource
import com.gggames.hourglass.features.games.data.remote.GamesDataSourceFake
import com.gggames.hourglass.features.games.data.remote.RemoteGamesDataSource
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class FakeGamesModule {

    @Provides
    @Singleton
    fun provideGamesRepository(
        repository: GamesRepositoryImpl
    ): GamesRepository = repository

    @Provides
    @Singleton
    fun provideRemoteGamesDataSource(
        dataSource: GamesDataSourceFake
    ): RemoteGamesDataSource = dataSource


    @Provides
    @Singleton
    fun provideInMemoryDataSource(
        dataSource: GamesMemoryDataSource
    ): InMemoryGamesDataSource = dataSource
}
