package com.gggames.hourglass.features.cards.data

import com.gggames.hourglass.model.Card
import io.reactivex.Completable
import io.reactivex.Observable
import javax.inject.Inject

class CardsRepositoryImpl @Inject constructor(
    private val firebaseCardsDataSource: CardsDataSource
) : CardsRepository {

    override fun getAllCards(): Observable<List<Card>> =
        firebaseCardsDataSource.getAllCards()

    override fun addCards(cards: List<Card>): Completable =
        firebaseCardsDataSource.addCards(cards)

    override fun updateCard(card: Card): Completable =
        firebaseCardsDataSource.update(card)

    override fun updateCards(cards: List<Card>): Completable =
        firebaseCardsDataSource.updateCards(cards)
}
