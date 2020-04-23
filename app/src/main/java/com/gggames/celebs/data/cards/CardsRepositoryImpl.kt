package com.gggames.celebs.data.cards

import com.gggames.celebs.data.model.Card
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

class CardsRepositoryImpl(
    private val firebaseCardsDataSource: CardsDataSource
) : CardsRepository {

    override fun getAllCards(): Observable<List<Card>> =
        firebaseCardsDataSource.getAllCards()

    override fun getMyCards(): Single<List<Card>> =
        firebaseCardsDataSource.getMyCards()

    override fun addCards(cards: List<Card>): Completable =
        firebaseCardsDataSource.addCards(cards)

    override fun updateCard(card: Card) : Completable =
        firebaseCardsDataSource.update(card)

    override fun updateCards(cards: List<Card>): Completable =
        firebaseCardsDataSource.updateCards(cards)
}