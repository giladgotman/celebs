package com.gggames.celebs.features.cards.data.remote

import com.gggames.celebs.model.Card
import org.junit.Before
import org.junit.Test

class CardsDataSourceFakeTest {

    lateinit var cardsDataSourceFake: CardsDataSourceFake

    @Before
    fun setup() {
        cardsDataSourceFake = CardsDataSourceFake()
    }

    @Test
    fun `given card is added When getAll is called Then card is returned`() {
        val id = "id"
        val card = createCard(id)
        cardsDataSourceFake.addCards(listOf(card)).blockingAwait()

        val cards = cardsDataSourceFake.getAllCards().test()
        cards.assertValueAt(0, listOf(card))
    }


    @Test
    fun `given card is updated When getAll is called Then updated card is returned`() {
        val id = "id"
        val card = createCard(id)
        cardsDataSourceFake.addCards(listOf(card)).blockingAwait()

        val cards = cardsDataSourceFake.getAllCards().test()
        cards.assertValueAt(0, listOf(card))

        cards.values().clear()

        val updatedCard = card.copy(used = true)
        cardsDataSourceFake.update(updatedCard).blockingAwait()

        cards.assertValueAt(0, listOf(updatedCard))
    }

    @Test
    fun `given carsd are updated When getAll is called Then updated cards are returned`() {
        val id = "id"
        val id2 = "id2"
        val card = createCard(id)
        val card2 = createCard(id2)
        cardsDataSourceFake.addCards(listOf(card, card2)).blockingAwait()

        val cards = cardsDataSourceFake.getAllCards().test()
        cards.assertValueAt(0, listOf(card, card2))

        cards.values().clear()

        val updatedCard = card.copy(used = true)
        cardsDataSourceFake.update(updatedCard).blockingAwait()

        cards.assertValueAt(0, listOf(updatedCard, card2))
    }

    private fun createCard(
         id: String = "id",
         name: String = "name",
         player: String = "player",
         used: Boolean = false,
         videoUrl1: String? = null,
         videoUrl2: String? = null,
         videoUrl3: String? = null,
         videoUrlFull: String? = null
    ) = Card(
        id, name, player, used, videoUrl1, videoUrl2, videoUrl3, videoUrlFull
    )

}