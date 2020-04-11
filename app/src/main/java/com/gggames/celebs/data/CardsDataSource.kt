package com.gggames.celebs.data

import com.gggames.celebs.data.model.Card
import io.reactivex.Completable
import io.reactivex.Single

interface CardsDataSource {
    fun getAllCards(): Single<List<Card>>

    fun addCards(cards: List<Card>): Completable
}