package com.gggames.celebs.features.cards.data.remote

import com.gggames.celebs.features.cards.data.CardsDataSource
import com.gggames.celebs.model.Card
import com.gggames.celebs.model.remote.CardRaw
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Observable.just
import io.reactivex.subjects.BehaviorSubject
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.properties.Delegates


@Singleton
class CardsDataSourceFake @Inject constructor() : CardsDataSource {

    private var cards: MutableList<Card> by Delegates.observable(mutableListOf()) { _, oldList, newList ->
        cardsSubject.onNext(newList)
    }
    private val cardsSubject = BehaviorSubject.createDefault<List<Card>>(emptyList())

    override fun getAllCards(): Observable<List<Card>> =
        cardsSubject

    override fun addCards(cards: List<Card>): Completable =
        Completable.fromCallable {
            val updatedList = this.cards
            updatedList.addAll(cards)

            this.cards = updatedList
            Unit
        }

    override fun update(card: Card): Completable =
        Completable.fromCallable {
            val updatedList = this.cards
            updatedList.indexOfFirst { it.name == card.name }.takeIf { it != -1 }?.let { index ->
                updatedList.set(index, card)
            } ?: updatedList.add(card)

            cards = updatedList
            Unit
        }

    override fun updateCards(cards: List<Card>): Completable =
        just(cards).flatMapIterable { it }
            .flatMapCompletable { update(it) }

    private fun getFakeCards(cards: List<CardRaw>): List<CardRaw> =
        cards.map {
            it.copy(
                videoUrl1 = "https://drive.google.com/uc?export=download&id=194rl8msLR47b8No3-uuI-AmLre2wgoC9",
                videoUrl2 = "https://drive.google.com/uc?export=download&id=147xu8GaVe25o3LhJ6xNcElqeEEHD6_vW",
                videoUrl3 = "https://drive.google.com/uc?export=download&id=1CGIg6YgKin7m-QmHvyQ03omj6yEvWFRG",
                videoUrlFull = "https://drive.google.com/uc?export=download&id=1k-6jLFqi7YO_QgeCfA_ubU22_vLY-2AO"

            )
        }
}