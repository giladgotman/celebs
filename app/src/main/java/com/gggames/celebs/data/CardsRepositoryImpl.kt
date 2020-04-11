package com.gggames.celebs.data

import com.gggames.celebs.data.model.Card
import io.reactivex.Completable
import io.reactivex.Single

class CardsRepositoryImpl(
    private val firebaseCardsDataSource: CardsDataSource
) : CardsRepository{
    override fun getAllCards(): Single<List<Card>> =
        firebaseCardsDataSource.getAllCards()

    override fun addCards(cards: List<Card>): Completable =
        firebaseCardsDataSource.addCards(cards)
}