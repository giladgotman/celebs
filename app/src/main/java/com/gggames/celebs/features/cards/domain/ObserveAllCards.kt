package com.gggames.celebs.features.cards.domain

import com.gggames.celebs.features.cards.data.CardsRepository
import com.gggames.celebs.presentation.gameon.GameScreenContract.Result.CardsUpdate
import io.reactivex.Observable
import javax.inject.Inject

class ObserveAllCards @Inject constructor(
    private val cardsRepository: CardsRepository
) {
    operator fun invoke(): Observable<CardsUpdate> =
        cardsRepository.getAllCards()
            .distinctUntilChanged()
            .map { CardsUpdate(it) }
}
