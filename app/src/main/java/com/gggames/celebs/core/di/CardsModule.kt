package com.gggames.celebs.core.di

import com.gggames.celebs.core.GameFlow
import com.gggames.celebs.features.cards.data.CardsDataSource
import com.gggames.celebs.features.cards.data.CardsRepository
import com.gggames.celebs.features.cards.data.CardsRepositoryImpl
import com.gggames.celebs.features.cards.data.remote.FirebaseCardsDataSource
import dagger.Module
import dagger.Provides
import javax.inject.Named
import javax.inject.Singleton


// TODO: 08.05.20 use @PerGame ?

@Module
class CardsModule {

    @Provides
    @Named("GameId")
    fun provideGameId(gameFlow: GameFlow) = gameFlow.currentGame!!.id

    @Provides
    @Singleton
    fun provideCardsRepository(
        repository: CardsRepositoryImpl
    ): CardsRepository = repository

    @Provides
    @Singleton
    fun provideCardsDataSource(
        dataSource: FirebaseCardsDataSource
    ): CardsDataSource = dataSource
}