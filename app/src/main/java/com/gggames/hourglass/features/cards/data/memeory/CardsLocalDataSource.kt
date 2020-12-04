package com.gggames.hourglass.features.cards.data.memeory

import com.gggames.hourglass.features.cards.data.CardsDataSource
import com.gggames.hourglass.model.Card
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Observable.just
import io.reactivex.Observable.merge
import io.reactivex.subjects.PublishSubject
import timber.log.Timber
import javax.inject.Inject
import kotlin.properties.Delegates

class CardsLocalDataSource @Inject constructor() : CardsDataSource {
    private var cardsCache: MutableList<Card> by Delegates.observable(mutableListOf()) { _, oldList, newList ->
        cardsSubject.onNext(newList)
    }
    private val cardsSubject: PublishSubject<List<Card>> = PublishSubject.create<List<Card>>()

    override fun getAllCards(): Observable<List<Card>> =
        merge(just(cardsCache), cardsSubject)

    override fun addCards(cards: List<Card>): Completable =
        Completable.fromCallable {
            val updatedList = (this.cardsCache + cards).toMutableList()
            this.cardsCache = updatedList
            Timber.i("sss addCards, size: ${cardsCache.size}")
            Unit
        }


    override fun update(card: Card): Completable =
        Completable.fromCallable {
            val updatedList = getUpdatedList(card)
            cardsCache = updatedList
            Timber.i("sss update, size: ${cardsCache.size}")
            Unit
        }

    private fun getUpdatedList(card: Card): MutableList<Card> {
        val updatedList = this.cardsCache
        updatedList.indexOfFirst { it.id == card.id }.takeIf { it != -1 }?.let { index ->
            updatedList.set(index, card)
        } ?: updatedList.add(card)
        return updatedList
    }

    override fun updateCards(cards: List<Card>): Completable =
        Observable.just(cards).flatMapIterable { it }
            .flatMapCompletable { update(it) }


    fun setCards(cards: List<Card>): Completable =
        Completable.fromCallable {
            cardsCache = cards.toMutableList()
            Unit
        }

}
