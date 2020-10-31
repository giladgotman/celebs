package com.gggames.hourglass.core.di

import com.gggames.hourglass.features.players.data.PlayersDataSource
import com.gggames.hourglass.features.players.data.PlayersRepository
import com.gggames.hourglass.features.players.data.PlayersRepositoryImpl
import com.gggames.hourglass.features.players.data.remote.PlayersDataSourceFake
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class PlayersModule {

    @Provides
    @Singleton
    fun providePlayersRepository(
        repository: PlayersRepositoryImpl
    ): PlayersRepository = repository

    @Provides
    @Singleton
    fun providePlayersDataSource(
        dataSource: PlayersDataSourceFake
    ): PlayersDataSource = dataSource
}
