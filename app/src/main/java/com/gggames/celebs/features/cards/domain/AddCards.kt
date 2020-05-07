package com.gggames.celebs.features.cards.domain

import com.gggames.celebs.features.cards.data.CardsRepository
import com.gggames.celebs.model.Card
import com.idagio.app.core.utils.rx.scheduler.BaseSchedulerProvider
import io.reactivex.Completable

class AddCards(
    private val cardsRepository: CardsRepository,
    private val schedulerProvider: BaseSchedulerProvider
) {
    operator fun invoke(cards: List<Card>): Completable =
        cardsRepository.addCards(cards)
            .compose(schedulerProvider.applyCompletableDefault())
}