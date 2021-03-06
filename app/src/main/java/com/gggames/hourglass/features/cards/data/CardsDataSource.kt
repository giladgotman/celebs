package com.gggames.hourglass.features.cards.data

import com.gggames.hourglass.model.Card
import io.reactivex.Completable
import io.reactivex.Observable

interface CardsDataSource {
    fun getAllCards(): Observable<List<Card>>

    fun addCards(cards: List<Card>): Completable

    fun update(card: Card): Completable

    fun updateCards(cards: List<Card>): Completable
}
