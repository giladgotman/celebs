package com.gggames.celebs.features.cards.data.remote

import com.gggames.celebs.features.cards.data.CardsDataSource
import com.gggames.celebs.model.Card
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Observable.just
import io.reactivex.Observable.merge
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject

class CardsDataSourceFake @Inject constructor() : CardsDataSource {

    private val cards = mutableListOf<Card>()
    private val cardsSubject = PublishSubject.create<List<Card>>()

    override fun getAllCards(): Observable<List<Card>> =
        merge(just(cards), cardsSubject)

    override fun addCards(cards: List<Card>): Completable =
        Completable.fromCallable {
            this.cards.addAll(cards)
            cardsSubject.onNext(this.cards)
        }

    override fun update(card: Card): Completable =
        Completable.fromCallable {
            if (cards.contains(card)) {
                cards[cards.indexOf(card)] = card
            } else {
                cards.add(card)
            }
        }

    override fun updateCards(cards: List<Card>): Completable =
        just(cards).flatMapIterable { it }
            .flatMapCompletable { update(it) }
}