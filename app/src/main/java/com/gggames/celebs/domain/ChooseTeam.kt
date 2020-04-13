package com.gggames.celebs.domain

import com.gggames.celebs.data.GamesRepository
import com.gggames.celebs.data.model.Player
import com.idagio.app.core.utils.rx.scheduler.BaseSchedulerProvider
import io.reactivex.Completable

class ChooseTeam(
    private val gamesRepository: GamesRepository,
    private val schedulerProvider: BaseSchedulerProvider
) {
    operator fun invoke(gameId: String, player: Player, teamName: String): Completable =
        gamesRepository.chooseTeam(gameId, player, teamName)
            .compose(schedulerProvider.applyDefault())
}