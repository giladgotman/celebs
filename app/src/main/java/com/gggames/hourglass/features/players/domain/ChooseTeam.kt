package com.gggames.hourglass.features.players.domain

import com.gggames.hourglass.features.players.data.PlayersRepository
import com.gggames.hourglass.model.Player
import com.idagio.app.core.utils.rx.scheduler.BaseSchedulerProvider
import io.reactivex.Completable
import javax.inject.Inject

class ChooseTeam @Inject constructor(
    private val playersRepository: PlayersRepository,
    private val schedulerProvider: BaseSchedulerProvider
) {
    operator fun invoke(gameId: String, player: Player, teamName: String): Completable =
        playersRepository.chooseTeam(gameId, player, teamName)
            .compose(schedulerProvider.applyCompletableDefault())
}
