package com.gggames.celebs.features.cards.data.remote

import com.gggames.celebs.features.cards.data.CardsDataSource
import com.gggames.celebs.model.Card
import io.reactivex.Completable
import io.reactivex.Observable
import javax.inject.Inject

class CardsDataSourceFake @Inject constructor() : CardsDataSource{
    override fun getAllCards(): Observable<List<Card>> {
        TODO("Not yet implemented")
    }

    override fun addCards(cards: List<Card>): Completable {
        TODO("Not yet implemented")
    }

    override fun update(card: Card): Completable {
        TODO("Not yet implemented")
    }

    override fun updateCards(cards: List<Card>): Completable {
        TODO("Not yet implemented")
    }
}