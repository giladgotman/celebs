package com.gggames.celebs.core.di

import com.gggames.celebs.features.players.data.PlayersDataSource
import com.gggames.celebs.features.players.data.PlayersRepository
import com.gggames.celebs.features.players.data.PlayersRepositoryImpl
import com.gggames.celebs.features.players.data.remote.FirebasePlayersDataSource
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
        dataSource: FirebasePlayersDataSource
    ): PlayersDataSource = dataSource
}