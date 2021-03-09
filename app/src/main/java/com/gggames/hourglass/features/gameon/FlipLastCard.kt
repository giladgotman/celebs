package com.gggames.hourglass.features.gameon

import com.gggames.hourglass.features.cards.data.CardsRepository
import com.gggames.hourglass.model.Card
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

