package com.gggames.hourglass.features.cards.domain

import com.gggames.hourglass.features.cards.data.CardsRepository
import com.gggames.hourglass.model.Card
import com.gggames.hourglass.model.Player
import com.idagio.app.core.utils.rx.scheduler.BaseSchedulerProvider
import io.reactivex.Single
import javax.inject.Inject

class GetMyCards @Inject constructor(
    private val cardsRepository: CardsRepository,
    private val schedulerProvider: BaseSchedulerProvider
) {
    operator fun invoke(player: Player): Single<List<Card>> {
        return cardsRepository.getAllCards().firstOrError().map { it.filter { card -> card.player == player.id } }
            .compose(schedulerProvider.applySingleDefault())
    }
}
