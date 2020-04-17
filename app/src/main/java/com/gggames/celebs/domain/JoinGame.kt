package com.gggames.celebs.domain

import com.gggames.celebs.data.PlayersRepository
import com.gggames.celebs.data.model.Player
import com.idagio.app.core.utils.rx.scheduler.BaseSchedulerProvider
import io.reactivex.Completable

class JoinGame(
    private val playersRepository: PlayersRepository,
    private val schedulerProvider: BaseSchedulerProvider
) {
    operator fun invoke(gameId: String, player: Player): Completable =
        playersRepository.addPlayer(gameId, player)
            .compose(schedulerProvider.applyDefault())
}