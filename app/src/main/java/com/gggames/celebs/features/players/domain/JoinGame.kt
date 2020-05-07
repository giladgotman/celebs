package com.gggames.celebs.features.players.domain

import com.gggames.celebs.model.Player
import com.gggames.celebs.features.players.data.PlayersRepository
import com.idagio.app.core.utils.rx.scheduler.BaseSchedulerProvider
import io.reactivex.Completable

class JoinGame(
    private val playersRepository: PlayersRepository,
    private val schedulerProvider: BaseSchedulerProvider
) {
    operator fun invoke(gameId: String, player: Player): Completable =
        playersRepository.addPlayer(gameId, player)
            .compose(schedulerProvider.applyCompletableDefault())
}