package com.gggames.celebs.features.cards.data.remote

import com.gggames.celebs.features.cards.data.CardsDataSource
import com.gggames.celebs.model.Card
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Observable.just
import io.reactivex.subjects.BehaviorSubject
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.properties.Delegates


@Singleton
class CardsDataSourceFake @Inject constructor() : CardsDataSource {

    private val fakeCards = mutableListOf(
        createCard("id1", "1111111111", "fakeId"),
        createCard("id2", "2222222", "fakeId"),
        createCard("id3", "333333333", "fakeId"),
        createCard("id4", "44444444", "fakeId"),
        createCard("id5", "5555555", "fakeId"),
        createCard("id6", "666666", "fakeId"),
        createCard("id7", "7777777", "fakeId"),
        createCard("id8", "899999999", "fakeId"),
        createCard("id9", "9999999999", "fakeId"),
        createCard("id10", "10", "fakeId"),
        createCard("id11", "11", "fakeId"),
        createCard("id12", "12", "fakeId"),
        createCard("id13", "13", "fakeId"),
        createCard("id14", "14", "fakeId")
    )


    private var cards: MutableList<Card> by Delegates.observable(fakeCards) { _, oldList, newList ->
        cardsSubject.onNext(newList)
    }
    private val cardsSubject = BehaviorSubject.createDefault<List<Card>>(cards)

    override fun getAllCards(): Observable<List<Card>> =
        cardsSubject

    override fun addCards(cards: List<Card>): Completable =
        Completable.fromCallable {
            val updatedList = this.cards
            cards.forEachIndexed { i, card ->
                val cardWithId = card.copy(id = "id_$i")
                updatedList.add(cardWithId)
            }

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

}

fun createCard(
    id: String = "id",
    name: String = "name",
    player: String = "player",
    used: Boolean = false
) = Card(
    id = id,
    name = name,
    player = player,
    used = used
)