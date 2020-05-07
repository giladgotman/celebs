package com.gggames.celebs.features.cards.domain

import com.gggames.celebs.features.cards.data.CardsRepository
import com.gggames.celebs.model.Card
import com.gggames.celebs.model.Player
import com.idagio.app.core.utils.rx.scheduler.BaseSchedulerProvider
import io.reactivex.Single

class GetMyCards(
    private val cardsRepository: CardsRepository,
    private val schedulerProvider: BaseSchedulerProvider
) {
    operator fun invoke(player: Player): Single<List<Card>> {
        return cardsRepository.getAllCards().firstOrError().map { it.filter {card -> card.player == player.id } }
            .compose(schedulerProvider.applySingleDefault())
    }
}