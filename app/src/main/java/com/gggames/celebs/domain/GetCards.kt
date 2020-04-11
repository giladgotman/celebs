package com.gggames.celebs.domain

import com.gggames.celebs.data.CardsRepository
import com.gggames.celebs.data.model.Card
import com.idagio.app.core.utils.rx.scheduler.BaseSchedulerProvider
import io.reactivex.Single

class GetCards(
    private val cardsRepository: CardsRepository,
    private val schedulerProvider: BaseSchedulerProvider
) {
    operator fun invoke(): Single<List<Card>> =
        cardsRepository.getAllCards()
        .compose(schedulerProvider.applySingleDefaultSchedulers())
}