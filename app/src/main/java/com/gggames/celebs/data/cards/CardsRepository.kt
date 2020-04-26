package com.gggames.celebs.data.cards

import com.gggames.celebs.data.model.Card
import io.reactivex.Completable
import io.reactivex.Observable

interface CardsRepository {
    fun getAllCards(): Observable<List<Card>>

    fun addCards(cards: List<Card>): Completable

    fun updateCard(card: Card): Completable

    fun updateCards(cards: List<Card>): Completable
}
