package com.gggames.hourglass.features.cards.data.memeory

import com.gggames.hourglass.features.cards.data.remote.createCard
import org.junit.Test

class CardsLocalDataSourceTest {

    val tested = CardsLocalDataSource()


    @Test
    fun `empty cache should emit empty list once`() {
        val observer = tested.getAllCards().test()
        observer.assertValueCount(1)
        observer.assertValue(emptyList())
    }

    @Test
    fun `When cards added Then new list is emitted once`() {
        val observer = tested.getAllCards().test()
        observer.assertValueCount(1)
        observer.assertValue(emptyList())

        val addedCards = listOf(createCard("id1"), createCard("id2"))
        observer.values().clear()
        tested.addCards(addedCards).blockingAwait()
        observer.assertValueCount(1)
        observer.assertValue(addedCards)
    }

    @Test
    fun `When card is updated Then list with updated card is emitted once`() {
        val observer = tested.getAllCards().test()
        val card1 = createCard("id1")
        val card2 = createCard("id2")
        val addedCards = listOf(card1, card2)

        tested.addCards(addedCards).blockingAwait()
        observer.values().clear()
        val updatedCard = card1.copy(name = "updated")

        tested.update(updatedCard).blockingAwait()
        observer.assertValueCount(1)
        observer.assertValue(listOf(updatedCard, card2))
    }

    @Test
    fun `When all cards are updated Then list with all updated cards is emitted once`() {
        val observer = tested.getAllCards().test()
        val card1 = createCard("id1")
        val card2 = createCard("id2")
        val addedCards = listOf(card1, card2)

        tested.addCards(addedCards).blockingAwait()
        observer.values().clear()
        val updatedCard1 = card1.copy(name = "updated")
        val updatedCard2 = card1.copy(name = "updated2")
        val updatedCards = listOf(updatedCard1, updatedCard2)

        tested.updateCards(updatedCards).blockingAwait()
        observer.assertValueCount(1)
        observer.assertValue(updatedCards)
    }

}