package com.gggames.celebs.data.cards

import com.gggames.celebs.data.model.Card
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

interface CardsRepository {
    fun getMyCards(): Single<List<Card>>

    fun getAllCards(): Observable<List<Card>>

    fun addCards(cards: List<Card>): Completable
    fun pickCard(): Card
}
