package com.gggames.hourglass.core.di

import com.gggames.hourglass.features.cards.data.CardsDataSource
import com.gggames.hourglass.features.cards.data.CardsRepository
import com.gggames.hourglass.features.cards.data.CardsRepositoryImpl
import com.gggames.hourglass.features.cards.data.remote.FirebaseCardsDataSource
import dagger.Module
import dagger.Provides
import javax.inject.Named

// TODO: 08.05.20 use @PerGame ?

@Module
class CardsModule(val gameId: String) {

    @Provides
    @Named("GameId")
    fun provideGameId() = gameId

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
