package com.gggames.hourglass.features.cards.domain

import com.gggames.hourglass.features.cards.data.CardsRepository
import com.gggames.hourglass.presentation.gameon.GameScreenContract.Result.CardsUpdate
import io.reactivex.Observable
import javax.inject.Inject

class ObserveAllCards @Inject constructor(
    private val cardsRepository: CardsRepository
) {
    operator fun invoke(): Observable<CardsUpdate> =
        cardsRepository.getAllCards()
            .map { CardsUpdate(it) }
}
