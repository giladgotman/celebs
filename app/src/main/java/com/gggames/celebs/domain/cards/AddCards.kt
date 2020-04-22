package com.gggames.celebs.domain.cards

import com.gggames.celebs.data.cards.CardsRepository
import com.gggames.celebs.data.model.Card
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