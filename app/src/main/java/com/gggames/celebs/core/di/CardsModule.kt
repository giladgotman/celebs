package com.gggames.celebs.core.di

import com.gggames.celebs.features.cards.data.CardsDataSource
import com.gggames.celebs.features.cards.data.CardsRepository
import com.gggames.celebs.features.cards.data.CardsRepositoryImpl
import com.gggames.celebs.features.cards.data.remote.FirebaseCardsDataSource
import com.gggames.celebs.features.games.data.GamesRepositoryImpl
import dagger.Module
import dagger.Provides
import javax.inject.Named

// TODO: 08.05.20 use @PerGame ?

@Module
class CardsModule {

    @Provides
    @Named("GameId")
    fun provideGameId(repository: GamesRepositoryImpl) = repository.gameId

    @Provides
    // TODO: 08.05.20 use @PerGame ?
    fun provideCardsRepository(
        repository: CardsRepositoryImpl
    ): CardsRepository = repository

    @Provides
    // TODO: 08.05.20 use @PerGame ?
    fun provideCardsDataSource(
        dataSource: FirebaseCardsDataSource
    ): CardsDataSource = dataSource
}
