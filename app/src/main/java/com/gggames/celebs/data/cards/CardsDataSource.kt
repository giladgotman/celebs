package com.gggames.celebs.data.cards

import com.gggames.celebs.data.model.Card
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

interface CardsDataSource {
    fun getMyCards(): Single<List<Card>>

    fun getAllCards(): Observable<List<Card>>

    fun addCards(cards: List<Card>): Completable
}