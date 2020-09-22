package com.gggames.celebs.features.gameon

import com.gggames.celebs.features.cards.data.CardsRepository
import com.gggames.celebs.model.Card
import io.reactivex.Completable
import io.reactivex.Completable.complete
import javax.inject.Inject

class FlipLastCard @Inject constructor(
    private val cardsRepository: CardsRepository
) {
    operator fun invoke(card: Card?): Completable {
        return card?.let {
            cardsRepository.updateCard(it.copy(used = false))
        } ?: complete()
    }
}

