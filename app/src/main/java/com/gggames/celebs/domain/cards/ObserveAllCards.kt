package com.gggames.celebs.domain.cards

import com.gggames.celebs.data.cards.CardsRepository
import com.gggames.celebs.data.model.Card
import com.idagio.app.core.utils.rx.scheduler.BaseSchedulerProvider
import io.reactivex.Observable

class ObserveAllCards(
    private val cardsRepository: CardsRepository,
    private val schedulerProvider: BaseSchedulerProvider
) {
    operator fun invoke(): Observable<List<Card>> =
        cardsRepository.getAllCards()
        .compose(schedulerProvider.applyDefault())
}