package com.gggames.hourglass.features.cards.domain

import com.gggames.hourglass.features.cards.data.CardsRepository
import com.gggames.hourglass.model.Card
import com.idagio.app.core.utils.rx.scheduler.BaseSchedulerProvider
import io.reactivex.Completable
import javax.inject.Inject

class AddCards @Inject constructor(
    private val cardsRepository: CardsRepository,
    private val schedulerProvider: BaseSchedulerProvider
) {
    operator fun invoke(cards: List<Card>): Completable =
        cardsRepository.addCards(cards)
            .compose(schedulerProvider.applyCompletableDefault())
}
