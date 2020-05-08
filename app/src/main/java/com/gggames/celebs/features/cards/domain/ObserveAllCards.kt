package com.gggames.celebs.features.cards.domain

import com.gggames.celebs.features.cards.data.CardsRepository
import com.gggames.celebs.model.Card
import com.idagio.app.core.utils.rx.scheduler.BaseSchedulerProvider
import io.reactivex.Observable
import javax.inject.Inject

class ObserveAllCards @Inject constructor(
    private val cardsRepository: CardsRepository,
    private val schedulerProvider: BaseSchedulerProvider
) {
    operator fun invoke(): Observable<List<Card>> =
        cardsRepository.getAllCards()
        .compose(schedulerProvider.applyDefault())
}