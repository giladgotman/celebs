package com.gggames.celebs.domain.players

import com.gggames.celebs.data.players.PlayersRepository
import com.gggames.celebs.data.model.Player
import com.idagio.app.core.utils.rx.scheduler.BaseSchedulerProvider
import io.reactivex.Completable

class ChooseTeam(
    private val playersRepository: PlayersRepository,
    private val schedulerProvider: BaseSchedulerProvider
) {
    operator fun invoke(gameId: String, player: Player, teamName: String): Completable =
        playersRepository.chooseTeam(gameId, player, teamName)
            .compose(schedulerProvider.applyCompletableDefault())
}